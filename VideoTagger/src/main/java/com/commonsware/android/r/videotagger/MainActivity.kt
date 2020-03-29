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

package com.commonsware.android.r.videotagger

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.*
import com.commonsware.android.r.videotagger.databinding.ActivityMainBinding
import com.commonsware.android.r.videotagger.databinding.RowBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val REQUEST_PERMS = 1337
private const val PERMS_READ = 1338

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()
  private lateinit var binding: ActivityMainBinding
  private lateinit var videosAdapter: VideosAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    LinearLayoutManager(this).let {
      binding.list.layoutManager = it
      binding.list.addItemDecoration(
        DividerItemDecoration(
          this,
          it.orientation
        )
      )
    }

    motor.states.observe(this) { state ->
      when (state) {
        MainViewState.Loading -> {
          binding.progress.visibility = View.VISIBLE
          binding.content.visibility = View.GONE
        }
        is MainViewState.Content -> {
          binding.progress.visibility = View.GONE
          binding.content.visibility = View.VISIBLE

          videosAdapter =
            VideosAdapter(layoutInflater).apply { submitList(state.models) }

          binding.list.adapter = videosAdapter

          binding.set.setOnClickListener {
            applyTags(state.models.filter { it.isChecked })
          }
        }
        MainViewState.Error -> {
          Toast.makeText(
            this,
            "Ick! Something went wrong! Check Logcat!",
            Toast.LENGTH_LONG
          ).show()
          finish()
        }
      }
    }

    loadVideos()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    when (requestCode) {
      REQUEST_PERMS -> {
        if (resultCode == Activity.RESULT_OK) {
          applyTags(videosAdapter.currentList.filter { it.isChecked })
        } else {
          finish()
        }
      }
      else -> {
        super.onActivityResult(requestCode, resultCode, data)
      }
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == PERMS_READ) {
      loadVideos()
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun loadVideos() {
    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      motor.loadVideos()
    } else {
      requestPermissions(
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        PERMS_READ
      )
    }
  }

  private fun applyTags(models: List<VideoModel>) {
    val needed = neededPermissions(models.map { it.uri })

    if (needed.isEmpty()) {
      motor.applyTags(models, binding.tags.text.toString())
    } else {
      val pi = MediaStore.createWriteRequest(contentResolver, needed)

      startIntentSenderForResult(pi.intentSender, REQUEST_PERMS, null, 0, 0, 0)
    }
  }

  private fun neededPermissions(selections: List<Uri>) =
    selections.filter {
      checkUriPermission(
        it,
        Process.myPid(),
        Process.myUid(),
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
      ) != PackageManager.PERMISSION_GRANTED
    }
}

private class VideosAdapter(private val layoutInflater: LayoutInflater) :
  ListAdapter<VideoModel, RowHolder>(VideoModelDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    RowHolder(RowBinding.inflate(layoutInflater, parent, false))

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

private class RowHolder(private val binding: RowBinding) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(model: VideoModel) {
    binding.title.text = model.title
    binding.tags.text = model.tags ?: "(no tags)"
    binding.selected.isChecked = model.isChecked

    binding.selected.setOnCheckedChangeListener { cb, isChecked ->
      model.isChecked = isChecked
    }
  }
}

private object VideoModelDiffer : DiffUtil.ItemCallback<VideoModel>() {
  override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel) =
    oldItem.uri == newItem.uri

  override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel) =
    oldItem.title == newItem.title &&
        oldItem.tags == newItem.tags &&
        oldItem.isChecked == newItem.isChecked
}
