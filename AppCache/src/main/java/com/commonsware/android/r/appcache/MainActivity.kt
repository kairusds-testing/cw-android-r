/*
  Copyright (c) 2020 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android R_

  https://commonsware.com/R
*/

package com.commonsware.android.r.appcache

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.commonsware.android.r.appcache.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)
  }

  override fun onStart() {
    super.onStart()

    if (hasAllFilesPermission()) {
      clearAppCacheMaybe()
    } else {
      val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

      startActivity(
        Intent(
          Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
          uri
        )
      )
    }
  }

  private fun hasAllFilesPermission() = Environment.isExternalStorageManager()

  private fun clearAppCacheMaybe() {
    startActivityForResult(Intent(StorageManager.ACTION_CLEAR_APP_CACHE), 1337)
  }
}
