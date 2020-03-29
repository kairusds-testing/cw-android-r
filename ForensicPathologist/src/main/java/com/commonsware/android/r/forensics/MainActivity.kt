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

package com.commonsware.android.r.forensics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.commonsware.android.r.forensics.databinding.ActivityMainBinding
import com.commonsware.android.r.forensics.databinding.RowBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    val adapter = ExitInfoAdapter(layoutInflater, this)

    binding.list.layoutManager = LinearLayoutManager(this)
    binding.list.adapter = adapter

    motor.content.observe(this) {
      if (it.isEmpty()) {
        binding.list.visibility = View.GONE
        binding.empty.visibility = View.VISIBLE
      } else {
        adapter.submitList(it)
      }
    }
  }
}

class ExitInfoAdapter(
  private val inflater: LayoutInflater,
  private val context: Context
) : ListAdapter<ExitInfo, RowHolder>(ExitInfoDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    RowHolder(RowBinding.inflate(inflater, parent, false), context)

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class RowHolder(private val binding: RowBinding, private val context: Context) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(info: ExitInfo) {
    binding.reason.text = context.getString(info.reason, info.status)
    binding.description.text = info.description
    binding.timestamp.text = info.timestamp
    binding.importance.text = context.getString(info.importance)
    binding.memory.text =
      context.getString(R.string.pss_rss, info.pss, info.rss)
  }
}

object ExitInfoDiffer : DiffUtil.ItemCallback<ExitInfo>() {
  override fun areItemsTheSame(oldItem: ExitInfo, newItem: ExitInfo) =
    oldItem === newItem

  override fun areContentsTheSame(oldItem: ExitInfo, newItem: ExitInfo) =
    oldItem == newItem
}