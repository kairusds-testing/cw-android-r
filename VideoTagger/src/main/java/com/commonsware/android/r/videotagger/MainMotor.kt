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

package com.commonsware.android.r.videotagger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class MainViewState {
  object Loading : MainViewState()
  data class Content(val models: List<VideoModel>) : MainViewState()
  object Error : MainViewState()
}

class MainMotor(private val repo: VideoRepository) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states

  fun loadVideos() {
    _states.value = MainViewState.Loading

    viewModelScope.launch(Dispatchers.Main) {
      try {
        _states.value = MainViewState.Content(repo.loadVideos())
      } catch (ex: Exception) {
        Log.e("MovieTagger", "Exception loading videos", ex)
        _states.value = MainViewState.Error
      }
    }
  }

  fun applyTags(models: List<VideoModel>, tags: String) {
    _states.value = MainViewState.Loading

    viewModelScope.launch(Dispatchers.Main) {
      repo.applyTags(models, tags)
      loadVideos()
    }
  }
}