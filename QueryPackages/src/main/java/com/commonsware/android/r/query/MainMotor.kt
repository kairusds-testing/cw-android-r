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

package com.commonsware.android.r.query

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private val PERMS = arrayOf(Manifest.permission.INTERNET)
private val RECEIVER_INTENT = Intent(Intent.ACTION_BOOT_COMPLETED)
private val LAUNCHER_INTENT =
  Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

data class MainViewState(
  val installedApps: List<ApplicationInfo>,
  val installedPackages: List<PackageInfo>,
  val internetPackages: List<PackageInfo>,
  val bootReceivers: List<ResolveInfo>,
  val providers: List<ProviderInfo>,
  val launcherActivities: List<ResolveInfo>
)

class MainMotor(private val context: Context) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states

  fun load(force: Boolean = false) {
    if (force || _states.value == null) {
      val pm = context.packageManager

      _states.value = MainViewState(
        installedApps = pm.getInstalledApplications(0),
        installedPackages = pm.getInstalledPackages(0),
        internetPackages = pm.getPackagesHoldingPermissions(PERMS, 0),
        bootReceivers = pm.queryBroadcastReceivers(RECEIVER_INTENT, 0),
        providers = pm.queryContentProviders(null, 0, 0).orEmpty(),
        launcherActivities = pm.queryIntentActivities(LAUNCHER_INTENT, 0)
      )
    }
  }
}