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

package com.commonsware.android.r.permcheck

import android.app.AppOpsManager
import android.app.Application
import android.app.AsyncNotedAppOp
import android.app.SyncNotedAppOp
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.concurrent.Executors

private const val TAG = "PermissionCheck"
private const val FEATURE_ID = "awesome-stuff"

class MainApp : Application() {
  private val module = module {
    viewModel { MainMotor(androidContext().createAttributionContext(FEATURE_ID)) }
  }
  private val executor = Executors.newSingleThreadExecutor()

  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@MainApp)
      modules(module)
    }

    getSystemService(AppOpsManager::class.java)
      ?.setOnOpNotedCallback(executor, object : AppOpsManager.OnOpNotedCallback() {
        override fun onNoted(op: SyncNotedAppOp) {
          Log.d(TAG, "onNoted: ${op.toDebugString()}")
          RuntimeException().printStackTrace(System.out)
        }

        override fun onSelfNoted(op: SyncNotedAppOp) {
          Log.d(TAG, "onSelfNoted: ${op.toDebugString()}")
          RuntimeException().printStackTrace(System.out)
        }

        override fun onAsyncNoted(op: AsyncNotedAppOp) {
          Log.d(TAG, "onAsyncNoted: ${op.toDebugString()}")
          RuntimeException().printStackTrace(System.out)
        }
      })
  }

  private fun SyncNotedAppOp.toDebugString() =
    "SyncNotedAppOp[attributionTag = $attributionTag, op = $op"

  private fun AsyncNotedAppOp.toDebugString() =
    "AsyncNotedAppOp[attributionTag = $attributionTag, op = $op, time = $time, uid = $notingUid, message = $message"
}