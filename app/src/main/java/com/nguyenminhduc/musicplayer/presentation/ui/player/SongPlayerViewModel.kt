package com.nguyenminhduc.musicplayer.presentation.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel

class SongPlayerViewModel(
    song: MusicFileUiModel,
    songList: List<MusicFileUiModel>
) : ViewModel() {

    private val _song = MutableLiveData(song)
    val song: LiveData<MusicFileUiModel> = _song

    private val _songList = MutableLiveData(songList)
    val songList: LiveData<List<MusicFileUiModel>> = _songList

    fun onNextClick() {
        val nextSongIndex = (_songList.value?.indexOf(_song.value)?.plus(1))?.mod(_songList.value?.size ?: 1) ?: 0
        _song.value = _songList.value?.get(nextSongIndex)
    }

    fun onPreviousClick() {
        val prevSongIndex = (_songList.value?.indexOf(_song.value)?.minus(1))?.takeIf { it >= 0 } ?: 0
        _song.value = _songList.value?.get(prevSongIndex)
    }
}