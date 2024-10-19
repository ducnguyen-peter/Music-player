package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongListViewModel : ViewModel() {

    private val _songList = MutableLiveData<List<MusicFileUiModel>>()
    val songList: LiveData<List<MusicFileUiModel>> = _songList

    fun fetchSongs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioList = arrayListOf<MusicFileUiModel>()
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
                        MusicFileUiMapper.map(
                            MusicFile(
                                album = album,
                                title = title,
                                artist = artist,
                                path = path,
                                duration = duration
                            )
                        )
                    )
                }
            }
            cursor?.close()
            _songList.postValue(audioList)
        }
    }
}