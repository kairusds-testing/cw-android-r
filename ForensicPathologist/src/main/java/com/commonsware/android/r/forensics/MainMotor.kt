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

package com.commonsware.android.r.forensics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ExitInfo(
  val description: String,
  @StringRes val importance: Int,
  val pss: Long,
  val rss: Long,
  @StringRes val reason: Int,
  val status: Int,
  val timestamp: CharSequence
)

class MainMotor(private val context: Context) : ViewModel() {
  private val _content = MutableLiveData<List<ExitInfo>>()
  val content: LiveData<List<ExitInfo>> = _content

  init {
    _content.value =
      context.getSystemService(ActivityManager::class.java)
        ?.getHistoricalProcessExitReasons(null, 0, 0).orEmpty()
        .map { convert(it) }
  }

  private fun convert(appExitInfo: ApplicationExitInfo): ExitInfo {
    return ExitInfo(
      description = appExitInfo.description.orEmpty(),
      importance = convertImportance(appExitInfo.importance),
      pss = appExitInfo.pss,
      rss = appExitInfo.rss,
      reason = convertReason(appExitInfo.reason),
      status = appExitInfo.status,
      timestamp = DateUtils.getRelativeTimeSpanString(
        context,
        appExitInfo.timestamp
      )
    )
  }

  @StringRes
  private fun convertImportance(importance: Int): Int = when (importance) {
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> R.string.importance_cached
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE -> R.string.importance_cant_save_state
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> R.string.importance_foreground
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE -> R.string.importance_foreground_service
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE -> R.string.importance_gone
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE -> R.string.importance_perceptible
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> R.string.importance_service
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING -> R.string.importance_top_sleeping
    ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> R.string.importance_visible
    else -> R.string.shrug
  }

  @StringRes
  private fun convertReason(reason: Int): Int = when (reason) {
    ApplicationExitInfo.REASON_ANR -> R.string.reason_anr
    ApplicationExitInfo.REASON_CRASH -> R.string.reason_crash
    ApplicationExitInfo.REASON_CRASH_NATIVE -> R.string.reason_crash_native
    ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> R.string.reason_excessive_resource_usage
    ApplicationExitInfo.REASON_EXIT_SELF -> R.string.reason_exit_self
    ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> R.string.reason_init_failure
    ApplicationExitInfo.REASON_LOW_MEMORY -> R.string.reason_low_memory
    ApplicationExitInfo.REASON_OTHER -> R.string.reason_other
    ApplicationExitInfo.REASON_PERMISSION_CHANGE -> R.string.reason_permission_change
    ApplicationExitInfo.REASON_SIGNALED -> R.string.reason_signaled
    else -> R.string.shrug
  }
}
