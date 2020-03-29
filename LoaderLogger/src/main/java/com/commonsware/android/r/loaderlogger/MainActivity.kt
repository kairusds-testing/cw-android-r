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

import android.content.res.loader.DirectoryAssetsProvider
import android.content.res.loader.ResourcesLoader
import android.content.res.loader.ResourcesProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.commonsware.android.r.loaderlogger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val assetsProvider =
      AssetsProviderWrapper(DirectoryAssetsProvider(getExternalFilesDir(null)!!))
    val resProvider = ResourcesProvider.empty(assetsProvider)
    val resLoader = ResourcesLoader().apply { addProvider(resProvider) }

    resources.addLoaders(resLoader)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    binding.asset.text = assets.open("stuff/awesome.txt").reader().readText()
    binding.raw.text = resources.openRawResource(R.raw.also_awesome).reader().readText()

    setContentView(binding.root)
  }
}
