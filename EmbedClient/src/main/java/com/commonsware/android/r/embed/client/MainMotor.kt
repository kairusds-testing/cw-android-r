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

package com.commonsware.android.r.embed.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.SurfaceControlViewHost
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val KEY_HOST_TOKEN = "hostToken"
private const val KEY_DISPLAY_ID = "displayId"
private const val KEY_WIDTH = "width"
private const val KEY_HEIGHT = "height"
private const val KEY_SURFACE_PACKAGE = "surfacePackage"

class MainMotor(private val context: Context) : ViewModel() {
  private var conn: MessengerConnection? = null
  private val _surfacePackage =
    MutableLiveData<SurfaceControlViewHost.SurfacePackage>()
  val surfacePackage: LiveData<SurfaceControlViewHost.SurfacePackage> =
    _surfacePackage
  private val handlerThread = HandlerThread("EmbedClient")
  private val handler: Handler
  private val messenger: Messenger

  init {
    handlerThread.start()
    handler = PackageHandler(handlerThread.looper) {
      _surfacePackage.postValue(it)
    }
    messenger = Messenger(handler)
  }

  fun bind(
    hostToken: IBinder?,
    displayId: Int,
    width: Int,
    height: Int
  ) {
    viewModelScope.launch {
      conn = bindToService()

      conn?.messenger?.send(Message.obtain().apply {
        data = bundleOf(
          KEY_DISPLAY_ID to displayId,
          KEY_WIDTH to width,
          KEY_HEIGHT to height
        ).apply {
          putBinder(KEY_HOST_TOKEN, hostToken)
        }
        replyTo = messenger
      })
    }
  }

  override fun onCleared() {
    super.onCleared()

    conn?.let { context.unbindService(it) }
  }

  private suspend fun bindToService(): MessengerConnection {
    return withContext(Dispatchers.Default) {
      suspendCoroutine<MessengerConnection> { continuation ->
        context.bindService(
          Intent().setClassName(
            "com.commonsware.android.r.embed.server",
            "com.commonsware.android.r.embed.server.ViewService"
          ),
          MessengerConnection { if (isActive) continuation.resume(it) },
          Context.BIND_AUTO_CREATE
        )
      }
    }
  }
}

private class MessengerConnection(private val onConnected: (MessengerConnection) -> Unit) :
  ServiceConnection {
  var messenger: Messenger? = null

  override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
    messenger = Messenger(binder)
    onConnected(this)
  }

  override fun onServiceDisconnected(name: ComponentName?) {
    messenger = null
  }
}

private class PackageHandler(
  looper: Looper,
  private val onPackageReceipt: (SurfaceControlViewHost.SurfacePackage) -> Unit
) : Handler(looper) {
  override fun handleMessage(msg: Message) {
    val pkg = msg.data.getParcelable<SurfaceControlViewHost.SurfacePackage>(
      KEY_SURFACE_PACKAGE
    )

    pkg?.let { onPackageReceipt(it) }
  }
}