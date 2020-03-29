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

package com.commonsware.android.r.rawpaths

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.*
import com.commonsware.android.r.rawpaths.databinding.ActivityMainBinding
import com.commonsware.android.r.rawpaths.databinding.RowBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val PERMS_STORAGE = 1337

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()
  private lateinit var volumeMenuItems: List<MenuItem>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    LinearLayoutManager(this).let {
      binding.list.layoutManager = it
      binding.list.addItemDecoration(
        DividerItemDecoration(
          this,
          it.orientation
        )
      )
    }

    val adapter = FilesAdapter(layoutInflater, ::onItemClick)

    binding.list.adapter = adapter

    lifecycleScope.launch {
      for (file in motor.copyErrors) {
        Toast.makeText(
          this@MainActivity,
          "Error copying $file",
          Toast.LENGTH_LONG
        ).show()
      }
    }

    motor.states.observe(this) { state ->
      when (state) {
        MainViewState.Loading -> binding.loading()

        is MainViewState.Content -> {
          binding.content()
          adapter.submitList(state.items)
        }

        MainViewState.Error -> binding.error()
      }
    }

    loadRoot()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == PERMS_STORAGE) {
      loadRoot()
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.actions, menu)

    val volumeMenu = menu.findItem(R.id.volumes).subMenu

    volumeMenuItems = motor.volumes.map {
      volumeMenu.add(it.getDescription(this)).apply {
        isCheckable = true
        isChecked = false
      }
    }

    volumeMenuItems.first().isChecked = true

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.allAccess) {
      if (Build.VERSION.SDK_INT >= 29 && Build.VERSION.PREVIEW_SDK_INT > 0) {
        if (hasAllFilesPermission()) {
          Toast.makeText(this, R.string.already_permission, Toast.LENGTH_LONG)
            .show()
        }

        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

        startActivity(
          Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            uri
          )
        )
      } else {
        Toast.makeText(this, R.string.sorry, Toast.LENGTH_LONG).show()
      }

      return true
    } else if (volumeMenuItems.contains(item) && !item.isChecked) {
      volumeMenuItems.forEach { it.isChecked = false }
      item.isChecked = true
      motor.loadRoot(volumeMenuItems.indexOf(item))
    }

    return super.onOptionsItemSelected(item)
  }

  private fun onItemClick(item: FileItem) {
    if (item.isDirectory) {
      motor.load(item.file)
    } else {
      Toast.makeText(this, item.crc32.toString(16), Toast.LENGTH_LONG).show()
      motor.copy(item.file)
    }
  }

  private fun loadRoot() {
    if (hasStoragePermission()) {
      motor.loadRoot()
    } else {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        PERMS_STORAGE
      )
    }
  }

  private fun ActivityMainBinding.loading() {
    progress.visibility = View.VISIBLE
    list.visibility = View.GONE
    error.visibility = View.GONE
  }

  private fun ActivityMainBinding.content() {
    progress.visibility = View.GONE
    list.visibility = View.VISIBLE
    error.visibility = View.GONE
  }

  private fun ActivityMainBinding.error() {
    progress.visibility = View.GONE
    list.visibility = View.GONE
    error.visibility = View.VISIBLE
  }

  private fun hasStoragePermission() =
    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun hasAllFilesPermission(): Boolean {
    val appOpsManager: AppOpsManager =
      getSystemService(AppOpsManager::class.java)!!

    return try {
      appOpsManager.unsafeCheckOpNoThrow(
        "android:manage_external_storage",
        android.os.Process.myUid(),
        packageName
      ) == AppOpsManager.MODE_ALLOWED
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
  }
}

class FilesAdapter(
  private val inflater: LayoutInflater,
  private val onClick: (FileItem) -> Unit
) :
  ListAdapter<FileItem, RowHolder>(FileItemDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    RowHolder(RowBinding.inflate(inflater, parent, false), onClick)

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class RowHolder(
  private val binding: RowBinding,
  private val onClick: (FileItem) -> Unit
) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(item: FileItem) {
    binding.icon.setImageResource(if (item.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)
    binding.name.text = item.file.name

    binding.root.setOnClickListener {
      onClick(item)
    }
  }
}

object FileItemDiffer : DiffUtil.ItemCallback<FileItem>() {
  override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem) =
    oldItem.file == newItem.file

  override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem) =
    oldItem.file.name == newItem.file.name && oldItem.isDirectory == newItem.isDirectory
}