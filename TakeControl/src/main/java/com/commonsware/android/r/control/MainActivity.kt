/*
  Copyright (c) 2020 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android R_

  https://commonsware.com/R
*/

package com.commonsware.android.r.control

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.commonsware.android.r.control.databinding.ActivityMainBinding

private const val EXTRA_MESSAGE = "message"

class MainActivity : AppCompatActivity() {
  companion object {
    fun buildIntent(context: Context, message: String) =
      Intent(context, MainActivity::class.java).putExtra(EXTRA_MESSAGE, message)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    intent.getStringExtra(EXTRA_MESSAGE)?.let { binding.message.text = it }
  }
}
