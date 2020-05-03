/*
  Copyright (c) 2019-2020 CommonsWare, LLC

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

package com.commonsware.android.r.bubble

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.JobIntentService
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.commonsware.android.r.bubble.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

private const val REQUEST_PICK = 1234

class MainActivity : AppCompatActivity() {
  private val workManager by lazy { WorkManager.getInstance(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    binding.show.setOnClickListener {
      workManager.enqueue(
        OneTimeWorkRequestBuilder<ShowNotificationWorker>()
          .setInitialDelay(10, TimeUnit.SECONDS)
          .build()
      )
      finish()
    }

    binding.cancel.setOnClickListener {
      getSystemService(NotificationManager::class.java).cancel(
        NOTIF_ID
      )
    }

    binding.showNow.setOnClickListener {
      showBubble(
        applicationContext
      )
    }

    binding.showService.setOnClickListener {
      startActivityForResult(
        Intent(
          ACTION_PICK,
          ContactsContract.Contacts.CONTENT_URI
        ), REQUEST_PICK
      )
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_PICK) {
      if (resultCode == Activity.RESULT_OK) {
        val intent = Intent(this, BubbleService::class.java)
          .setData(data?.data)

        JobIntentService.enqueueWork(
          this,
          BubbleService::class.java,
          1337,
          intent
        )
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }
}
