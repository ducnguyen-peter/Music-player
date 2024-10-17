package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile

class SongListViewModel: ViewModel() {

    private val _songList = MutableLiveData<List<MusicFile>>()
    val songList: LiveData<List<MusicFile>> = _songList

    fun fetchSongs(context: Context) {
        val audioList = arrayListOf<MusicFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // path
            MediaStore.Audio.Media.ARTIST
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            while (cursor.moveToNext()) {
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)
                audioList.add(
                    MusicFile(
                        album = album, title = title, artist = artist, path = path, duration = duration
                    )
                )
            }
        }
        cursor?.close()
        _songList.value = audioList
    }
}