package com.nguyenminhduc.musicplayer.data.query

import android.content.ContentResolver
import android.provider.MediaStore
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.model.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicFileQueryService {

    fun getLocalMusicFiles(contentResolver: ContentResolver, pagingState: PagingState): Flow<List<MusicFile>> = flow {
        val audioList = arrayListOf<MusicFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // path
            MediaStore.Audio.Media.ARTIST
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            while (cursor.moveToNext() && cursor.position < pagingState.pageSize) {
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getLong(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)
                audioList.add(
                    MusicFile(
                        album = album,
                        title = title,
                        artist = artist,
                        path = path,
                        duration = duration
                    )
                )
            }
        }
        cursor?.close()
        emit(audioList)
    }
}