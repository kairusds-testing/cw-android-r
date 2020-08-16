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

package com.commonsware.android.r.dynfilter

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
  private lateinit var nav: NavController
  private lateinit var appBarCfg: AppBarConfiguration
  private val mimeTypesKey by lazy { getString(R.string.mimeTypesKey) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    nav = supportFragmentManager.findFragmentById(R.id.nav_host)
      ?.findNavController()!!
    appBarCfg = AppBarConfiguration.Builder(nav.graph).build()

    NavigationUI.setupActionBarWithNavController(this, nav, appBarCfg)

    PreferenceManager.getDefaultSharedPreferences(this).apply {
      registerOnSharedPreferenceChangeListener { prefs, key ->
        if (key == mimeTypesKey) {
          updateMimeGroup(prefs)
        }
      }

      updateMimeGroup(this)
    }

    if (intent.action == Intent.ACTION_VIEW) {
      Toast.makeText(this, intent.toString(), Toast.LENGTH_LONG).show()
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    Toast.makeText(this, intent.toString(), Toast.LENGTH_LONG).show()
  }

  override fun onSupportNavigateUp() =
    NavigationUI.navigateUp(nav, appBarCfg) || super.onSupportNavigateUp()

  private fun updateMimeGroup(prefs: SharedPreferences) {
    val types = prefs.getStringSet(mimeTypesKey, emptySet()).orEmpty()

    packageManager.setMimeGroup("testMimeGroup", types)
  }
}
