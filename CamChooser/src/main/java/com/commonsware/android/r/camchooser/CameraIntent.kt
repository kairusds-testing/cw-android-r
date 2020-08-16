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

import android.content.Context
import android.content.Intent


private val CAMERA_CANDIDATES = listOf(
  "net.sourceforge.opencamera"
)

fun enhanceCameraIntent(
  context: Context,
  baseIntent: Intent,
  title: String
): Intent {
  val pm = context.packageManager

  val cameraIntents =
    CAMERA_CANDIDATES.map { Intent(baseIntent).setPackage(it) }
      .filter { pm.queryIntentActivities(it, 0).isNotEmpty() }
      .toTypedArray()

  return if (cameraIntents.isEmpty()) {
    baseIntent
  } else {
    Intent
      .createChooser(baseIntent, title)
      .putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents)
  }
}
