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
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

const val NOTIF_ID = 1337
const val CHANNEL_WHATEVER = "whatever"

fun showBubble(appContext: Context) {
  val pi = PendingIntent.getActivity(
    appContext,
    0,
    Intent(appContext, BubbleActivity::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT
  )

  val bubble = NotificationCompat.BubbleMetadata.Builder()
    .setDesiredHeight(400)
    .setIcon(IconCompat.createWithResource(appContext, R.mipmap.ic_launcher))
    .setIntent(pi)
    .setSuppressNotification(true)
    .build()

  val person = Person.Builder()
    .setBot(true)
    .setName("A Test Bot")
    .setImportant(true)
    .build()

  val builder = NotificationCompat.Builder(
    appContext,
    CHANNEL_WHATEVER
  )
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("Um, hi!")
    .setBubbleMetadata(bubble)

  val style = NotificationCompat.MessagingStyle(person)
    .setConversationTitle("A Fake Chat")

  style.addMessage("Want to chat?", System.currentTimeMillis(), person)
  builder.setStyle(style)

  NotificationManagerCompat.from(appContext).let { mgr ->
    mgr.createNotificationChannel(
      NotificationChannel(
        CHANNEL_WHATEVER,
        "Whatever",
        NotificationManager.IMPORTANCE_DEFAULT
      )
    )

    mgr.notify(NOTIF_ID, builder.build())
  }
}

class ShowNotificationWorker(
  private val appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
  override fun doWork(): Result {
    showBubble(appContext)

    return Result.success()
  }
}