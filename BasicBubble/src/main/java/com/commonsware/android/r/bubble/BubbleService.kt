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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_CALL
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat

class BubbleService : JobIntentService() {
  override fun onHandleWork(intent: Intent) {
    val pi = PendingIntent.getActivity(
      this,
      0,
      Intent(this, BubbleActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    val bubble = NotificationCompat.BubbleMetadata.Builder()
      .setDesiredHeight(400)
      .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
      .setIntent(pi)
      .setSuppressNotification(true)
      .build()

    val builder = NotificationCompat.Builder(
      this,
      CHANNEL_WHATEVER
    )
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Um, hi!")
      .setCategory(CATEGORY_CALL)
      .setBubbleMetadata(bubble)
      .addPerson(intent.data.toString())

    NotificationManagerCompat.from(this)
      .createNotificationChannel(
        NotificationChannel(
          CHANNEL_WHATEVER,
          "Whatever",
          NotificationManager.IMPORTANCE_DEFAULT
        )
      )

    startForeground(1337, builder.build())
  }
}