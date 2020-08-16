/*
  Copyright (c) 2020 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android R__

  https://commonsware.com/R
*/

package com.commonsware.android.r.camchooser

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.commonsware.android.r.camchooser.databinding.ActivityMainBinding
import java.io.File

private const val REQUEST_IMAGE = 1337
private const val REQUEST_IMAGE_SECURE = 1338
private const val REQUEST_VIDEO = 1339
private const val REQUEST_IMAGE_OUTPUT = 1437
private const val REQUEST_IMAGE_SECURE_OUTPUT = 1438
private const val REQUEST_VIDEO_OUTPUT = 1439
private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var testFile: File

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    testFile = File(cacheDir, "testfile")

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.image.setOnClickListener {
      test(MediaStore.ACTION_IMAGE_CAPTURE, REQUEST_IMAGE)
    }

    binding.imageSecure.setOnClickListener {
      test(MediaStore.ACTION_IMAGE_CAPTURE_SECURE, REQUEST_IMAGE_SECURE)
    }

    binding.video.setOnClickListener {
      test(MediaStore.ACTION_VIDEO_CAPTURE, REQUEST_VIDEO)
    }

    binding.imageOutput.setOnClickListener {
      test(MediaStore.ACTION_IMAGE_CAPTURE, REQUEST_IMAGE_OUTPUT, true)
    }

    binding.imageSecureOutput.setOnClickListener {
      test(
        MediaStore.ACTION_IMAGE_CAPTURE_SECURE,
        REQUEST_IMAGE_SECURE_OUTPUT,
        true
      )
    }

    binding.videoOutput.setOnClickListener {
      test(MediaStore.ACTION_VIDEO_CAPTURE, REQUEST_VIDEO_OUTPUT, true)
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        REQUEST_IMAGE -> validateImageResult(
          MediaStore.ACTION_IMAGE_CAPTURE,
          data
        )
        REQUEST_IMAGE_SECURE -> validateImageResult(
          MediaStore.ACTION_IMAGE_CAPTURE_SECURE,
          data
        )
        REQUEST_VIDEO -> validateVideoResult(
          MediaStore.ACTION_VIDEO_CAPTURE,
          data
        )
        REQUEST_IMAGE_OUTPUT -> validateUriResult(MediaStore.ACTION_IMAGE_CAPTURE)
        REQUEST_IMAGE_SECURE_OUTPUT -> validateUriResult(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
        REQUEST_VIDEO_OUTPUT -> validateUriResult(MediaStore.ACTION_VIDEO_CAPTURE)
        else -> super.onActivityResult(requestCode, resultCode, data)
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun test(
    action: String,
    requestCode: Int,
    includeUri: Boolean = false
  ) {
    try {
      val baseIntent = Intent(action)

      if (includeUri) {
        baseIntent
          .putExtra(
            MediaStore.EXTRA_OUTPUT,
            FileProvider.getUriForFile(this, AUTHORITY, testFile)
          )
          .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      }

      startActivityForResult(
        enhanceCameraIntent(this, baseIntent, action),
        requestCode
      )
    } catch (ex: ActivityNotFoundException) {
      showResult(action, "Activity not found!")
    }
  }

  private fun validateImageResult(action: String, data: Intent?) {
    when {
      data == null -> showResult(action, "No result provided")
      data.getParcelableExtra<Bitmap>("data") == null -> showResult(
        action,
        "No bitmap provided"
      )
      else -> showResult(action, "Probably OK!")
    }
  }

  private fun validateVideoResult(action: String, data: Intent?) {
    when {
      data == null -> showResult(action, "No result provided")
      data.data == null -> showResult(action, "No Uri provided")
      else -> showResult(action, "Probably OK!")
    }
  }

  private fun validateUriResult(action: String) {
    when {
      !testFile.exists() -> showResult(action, "Test file does not exist")
      testFile.length() == 0L -> showResult(action, "Test file has zero length")
      else -> showResult(action, "Probably OK!")
    }

    if (testFile.exists()) testFile.delete()
  }

  private fun showResult(action: String, message: String) {
    binding.scenario.text = action
    binding.result.text = message
  }
}
