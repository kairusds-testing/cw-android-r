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

package com.commonsware.android.r.loaderlogger

import android.content.res.loader.AssetsProvider
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.InputStream

class AssetsProviderWrapper(
  private val delegate: AssetsProvider,
  private val tag: String = "LoaderLogger"
) : AssetsProvider {
  override fun loadAsset(path: String, accessMode: Int): InputStream? {
    val result = delegate.loadAsset(path, accessMode)

    Log.i(tag, "loadAsset(path = $path, accessMode = $accessMode) -> $result")

    return result
  }

  override fun loadAssetParcelFd(path: String): ParcelFileDescriptor? {
    val result = loadAssetParcelFd(path)

    Log.i(tag, "loadAssetParcelFd(path = $path) -> $result")

    return result
  }
}