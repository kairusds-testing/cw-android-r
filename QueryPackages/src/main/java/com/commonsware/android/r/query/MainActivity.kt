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

import android.content.pm.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.commonsware.android.r.query.databinding.ActivityMainBinding
import com.commonsware.android.r.query.databinding.HeaderRowBinding
import com.commonsware.android.r.query.databinding.RowBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()

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

    motor.states.observe(this) { state ->
      val config =
        MergeAdapter.Config.Builder().setIsolateViewTypes(false).build()
      val inflater = layoutInflater
      val pm = packageManager

      binding.list.adapter = MergeAdapter(
        config,
        ApplicationAdapter(inflater, pm, state.installedApps),
        PackageAdapter(
          R.string.packages,
          inflater,
          pm,
          state.installedPackages
        ),
        PackageAdapter(R.string.internet, inflater, pm, state.internetPackages),
        ResolveAdapter(R.string.boot, inflater, pm, state.bootReceivers),
        ResolveAdapter(
          R.string.launcher,
          inflater,
          pm,
          state.launcherActivities
        ),
        ProviderAdapter(inflater, pm, state.providers)
      )
    }

    motor.load()
  }
}

abstract class HeaderAdapter<T>(
  @StringRes private val headerTitle: Int,
  private val inflater: LayoutInflater,
  private val pm: PackageManager,
  private val items: List<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  abstract fun bind(holder: RowHolder, item: T)

  override fun getItemViewType(position: Int) =
    if (position == 0) R.layout.header_row else R.layout.row

  override fun getItemCount() = items.size

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    return if (viewType == R.layout.header_row) {
      HeaderHolder(HeaderRowBinding.inflate(inflater, parent, false))
    } else {
      RowHolder(RowBinding.inflate(inflater, parent, false), pm)
    }
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    if (position == 0) {
      (holder as HeaderHolder).bind(headerTitle)
    } else {
      bind((holder as RowHolder), items[position - 1])
    }
  }
}

class ApplicationAdapter(
  inflater: LayoutInflater,
  pm: PackageManager,
  items: List<ApplicationInfo>
) : HeaderAdapter<ApplicationInfo>(R.string.apps, inflater, pm, items) {
  override fun bind(holder: RowHolder, item: ApplicationInfo) {
    holder.bind(item)
  }
}

class PackageAdapter(
  @StringRes headerTitle: Int,
  inflater: LayoutInflater,
  pm: PackageManager,
  items: List<PackageInfo>
) : HeaderAdapter<PackageInfo>(headerTitle, inflater, pm, items) {
  override fun bind(holder: RowHolder, item: PackageInfo) {
    holder.bind(item)
  }
}

class ResolveAdapter(
  @StringRes headerTitle: Int,
  inflater: LayoutInflater,
  pm: PackageManager,
  items: List<ResolveInfo>
) : HeaderAdapter<ResolveInfo>(headerTitle, inflater, pm, items) {
  override fun bind(holder: RowHolder, item: ResolveInfo) {
    holder.bind(item)
  }
}

class ProviderAdapter(
  inflater: LayoutInflater,
  pm: PackageManager,
  items: List<ProviderInfo>
) : HeaderAdapter<ProviderInfo>(R.string.providers, inflater, pm, items) {
  override fun bind(holder: RowHolder, item: ProviderInfo) {
    holder.bind(item)
  }
}

class HeaderHolder(private val binding: HeaderRowBinding) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(@StringRes title: Int) {
    binding.title.setText(title)
  }
}

class RowHolder(
  private val binding: RowBinding,
  private val pm: PackageManager
) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(info: ApplicationInfo) {
    binding.title.text = info.loadLabel(pm)
  }

  fun bind(info: PackageInfo) {
    binding.title.text = info.packageName
  }

  fun bind(info: ResolveInfo) {
    binding.title.text = info.loadLabel(pm)
  }

  fun bind(info: ProviderInfo) {
    binding.title.text = info.authority
  }
}