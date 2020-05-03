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

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PROJECTION = arrayOf(
  MediaStore.Video.Media._ID,
  MediaStore.Video.Media.DISPLAY_NAME,
  MediaStore.Video.Media.TAGS,
  MediaStore.Video.Media.DESCRIPTION
)
private const val SORT_ORDER = MediaStore.Video.Media.TITLE

class VideoRepository(context: Context) {
  private val resolver = context.contentResolver

  suspend fun loadVideos(): List<VideoModel> =
    withContext(Dispatchers.IO) {
      val collection =
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

      resolver.query(collection, PROJECTION, null, null, SORT_ORDER)
        ?.use { cursor ->
          cursor.mapToList {
            VideoModel(
              uri = MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL,
                it.getLong(0)
              ),
              title = it.getString(1),
              tags = it.getString(2),
              description = it.getString(3)
            )
          }
        } ?: emptyList()
    }

  suspend fun updateInfo(
    models: List<VideoModel>,
    tags: String
  ) =
    withContext(Dispatchers.IO) {
      val netTags = if (tags.isEmpty()) null else tags
      val values = ContentValues().apply {
        put(MediaStore.Video.Media.TAGS, netTags)
        put(MediaStore.Video.Media.DESCRIPTION, "a test description")
      }

      models.forEach { setMetadata(it.uri, values) }
    }

  private suspend fun setMetadata(uri: Uri, values: ContentValues) =
    withContext(Dispatchers.IO) {
      val tempValues = ContentValues().apply {
        put(MediaStore.Video.Media.IS_PENDING, 1)
      }

      resolver.update(uri, tempValues, null, null)

      val updatedValues = ContentValues(values).apply {
        put(MediaStore.Video.Media.IS_PENDING, 0)
      }

      resolver.update(uri, updatedValues, null, null)
    }

  private fun <T : Any> Cursor.mapToList(predicate: (Cursor) -> T): List<T> =
    generateSequence { if (moveToNext()) predicate(this) else null }
      .toList()
}
