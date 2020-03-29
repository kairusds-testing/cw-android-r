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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.function.Consumer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class MainViewState {
  data class Content(
    val hasPermission: Boolean,
    val location: Location? = null
  ) :
    MainViewState()

  object Error : MainViewState()
}

class MainMotor(private val context: Context) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states

  fun checkPermission() {
    _states.value = MainViewState.Content(hasLocationPermission())
  }

  fun fetchLocation() {
    viewModelScope.launch(Dispatchers.Main) {
      _states.value =
        MainViewState.Content(hasLocationPermission(), fetchLocationAsync())
    }
  }

  private fun hasLocationPermission() =
    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

  private suspend fun fetchLocationAsync(): Location {
    val locationManager =
      context.getSystemService(LocationManager::class.java)!!
    val executor = Executors.newSingleThreadExecutor()

    return withContext(executor.asCoroutineDispatcher()) {
      suspendCoroutine<Location> { continuation ->
        val consumer =
          Consumer<Location> { location ->
            if (isActive) continuation.resume(location)
          }

        locationManager.getCurrentLocation(
          LocationManager.GPS_PROVIDER,
          null,
          executor,
          consumer
        )
      }
    }
  }
}