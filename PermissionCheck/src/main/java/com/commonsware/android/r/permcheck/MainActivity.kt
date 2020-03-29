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
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.observe
import com.commonsware.android.r.permcheck.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val PERMS_LOCATION = 1337

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    title = getString(R.string.title, hashCode())

    motor.states.observe(this) { state ->
      when (state) {
        is MainViewState.Content -> {
          binding.permission.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !state.hasPermission) {
              ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMS_LOCATION
              )
            }
          }

          if (binding.permission.isChecked != state.hasPermission) {
            binding.permission.isChecked = state.hasPermission
          }

          binding.permission.isEnabled = !state.hasPermission
          binding.requestLocation.isEnabled = state.hasPermission
          binding.location.text = state.location?.displayString ?: "(no location)"

          binding.requestLocation.setOnClickListener { motor.fetchLocation() }
        }
        MainViewState.Error -> {
          Toast.makeText(this, R.string.error_toast, Toast.LENGTH_LONG).show()
          finish()
        }
      }
    }

    binding.launch.setOnClickListener { startActivity(Intent(this, this.javaClass)) }
  }

  override fun onResume() {
    super.onResume()

    motor.checkPermission()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == PERMS_LOCATION) {
      motor.checkPermission()
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private val Location.displayString
    get() = getString(R.string.location, latitude, longitude)
}
