/*
  Copyright (c) 2020 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android R

  https://commonsware.com/R
*/

package com.commonsware.android.r.rawpaths

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.CRC32

data class FileItem(
  val file: File,
  val crc32: Long = 0,
  val isDirectory: Boolean = false
)

sealed class MainViewState {
  object Loading : MainViewState()
  data class Content(val items: List<FileItem>) : MainViewState()
  object Error : MainViewState()
}

class MainMotor(context: Context) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states
  val copyErrors = Channel<File>(4)
  val volumes: List<StorageVolume> =
    context.getSystemService(StorageManager::class.java)!!.storageVolumes

  fun load(dir: File) {
    _states.postValue(MainViewState.Loading)

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val items = dir.listFiles().orEmpty()
          .sortedBy { it.name }
          .map { file ->
            if (file.isDirectory) {
              FileItem(file, isDirectory = true)
            } else {
              FileItem(file,
                crc32 = CRC32().let { crc ->
                  crc.update(file.readBytes())
                  crc.value
                })
            }
          }

        _states.postValue(MainViewState.Content(items))
      } catch (t: Throwable) {
        Log.e("RawPaths", "Exception loading $dir", t)
        _states.postValue(MainViewState.Error)
      }
    }
  }

  fun loadRoot(volumeIndex: Int = 0) {
    if (Build.VERSION.SDK_INT <= 30) {
      load(Environment.getExternalStorageDirectory())
    } else {
      load(volumes[volumeIndex].directory!!)
    }
  }

  fun copy(source: File) {
    viewModelScope.launch(Dispatchers.IO) {
      source.parentFile?.let { parent ->
        try {
          val dest = File(
            parent,
            "${source.nameWithoutExtension}-copy.${source.extension}"
          )

          source.copyTo(dest, overwrite = false)
          load(parent)
        } catch (t: Throwable) {
          Log.e("RawPaths", "Exception copying $source", t)
          copyErrors.offer(source)
        }
      }
    }
  }
}