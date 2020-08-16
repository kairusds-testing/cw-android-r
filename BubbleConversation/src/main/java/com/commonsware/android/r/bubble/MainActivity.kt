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

package com.commonsware.android.r.bubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.commonsware.android.r.bubble.databinding.ActivityMainBinding

private const val NOTIF_ID = 1337
private const val CHANNEL_WHATEVER = "whatever"
private const val SHORTCUT_ID = "something.unique"

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    binding.start.setOnClickListener { showBubble(this) }
  }

  private fun buildBubbleNotification(appContext: Context, showExpanded: Boolean = false): Notification {
    val pi = PendingIntent.getActivity(
      appContext,
      0,
      Intent(appContext, BubbleActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    val bubble = NotificationCompat.BubbleMetadata.Builder()
      .setDesiredHeight(400)
      .setIcon(IconCompat.createWithResource(appContext, R.drawable.ic_two))
      .setIntent(pi)
      .apply { if (showExpanded) setAutoExpandBubble(true); setSuppressNotification(true) }
      .build()

    val shortcutInfo = ShortcutInfoCompat.Builder(this, SHORTCUT_ID)
      .setLongLived(true)
      .setShortLabel("Settings")
      .setIntent(Intent(Settings.ACTION_SETTINGS))
      .setIcon(IconCompat.createWithResource(this, R.drawable.ic_one))
      .build()

    ShortcutManagerCompat.pushDynamicShortcut(this, shortcutInfo)

    val builder = NotificationCompat.Builder(
      appContext,
      CHANNEL_WHATEVER
    )
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Um, hi!")
      .setBubbleMetadata(bubble)
      .setShortcutInfo(shortcutInfo)

    val person = Person.Builder()
      .setBot(true)
      .setName("A Test Bot")
      .setImportant(true)
      .build()

    val style = NotificationCompat.MessagingStyle(person)
      .setConversationTitle("A Fake Chat")

    style.addMessage("Want to chat?", System.currentTimeMillis(), person)
    builder.setStyle(style)

    return builder.build()
  }

  private fun showBubble(appContext: Context, showExpanded: Boolean = false) {
    NotificationManagerCompat.from(appContext).let { mgr ->
      mgr.createNotificationChannel(
        NotificationChannel(
          CHANNEL_WHATEVER,
          "Whatever",
          NotificationManager.IMPORTANCE_DEFAULT
        )
      )

      mgr.notify(NOTIF_ID, buildBubbleNotification(appContext, showExpanded))
    }
  }
}
