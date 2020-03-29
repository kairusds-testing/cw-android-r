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

package com.commonsware.android.r.bgloc

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.commonsware.android.r.bgloc.databinding.ActivityMainBinding

private const val PERMS_LOCATION = 1337

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    getPermission()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == PERMS_LOCATION) {
      if (grantResults.first() == PackageManager.PERMISSION_GRANTED) getPermission()
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun getPermission() {
    when {
      !hasLocationPermission() -> requestLocationPermission()
      !hasBackgroundPermission() -> requestBackgroundPermission()
      else -> {
        binding.status.setText(R.string.bg_perm)
        binding.background.visibility = View.GONE
      }
    }
  }

  private fun requestLocationPermission() {
    binding.status.setText(R.string.no_perms)
    binding.background.visibility = View.INVISIBLE

    ActivityCompat.requestPermissions(
      this,
      arrayOf(ACCESS_FINE_LOCATION),
      PERMS_LOCATION
    )
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun requestBackgroundPermission() {
    binding.status.setText(R.string.fine_perm)
    binding.background.visibility = View.VISIBLE

    binding.background.setOnClickListener {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(ACCESS_BACKGROUND_LOCATION),
        PERMS_LOCATION
      )
    }
  }

  private fun hasLocationPermission() =
    checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

  private fun hasBackgroundPermission() = Build.VERSION.SDK_INT < 29 ||
      checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
}
