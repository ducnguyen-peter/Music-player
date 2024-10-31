package com.nguyenminhduc.musicplayer.presentation.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nguyenminhduc.musicplayer.data.pref.SongPlayerSharedPref
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.utils.orFalse
import kotlin.random.Random

class SongPlayerViewModel(
    songPosition: Int,
    songList: List<MusicFileUiModel>,
    private val sharedPref: SongPlayerSharedPref
) : ViewModel() {

    private val _song = MutableLiveData(songList.get(songPosition))
    val song: LiveData<MusicFileUiModel> = _song

    private val _songList = MutableLiveData(songList)
    val songList: LiveData<List<MusicFileUiModel>> = _songList

    private val _isShuffling = MutableLiveData(sharedPref.getIsShuffling())
    val isShuffling: LiveData<Boolean> = _isShuffling

    private val _isRepeating = MutableLiveData(sharedPref.getIsRepeating())
    val isRepeating: LiveData<Boolean> = _isRepeating

    fun onNextClick() {
        _song.value = _songList.value?.get(getNextSongIndex())
    }

    fun onAutoNext() {
        _song.value = if (!_isRepeating.value.orFalse())
            _songList.value?.get(getNextSongIndex())
        else _song.value
    }

    fun onPreviousClick() {
        val prevSongIndex = (_songList.value.orEmpty().indexOf(_song.value) - 1).takeIf { it >= 0 } ?: 0
        _song.value = _songList.value?.get(prevSongIndex)
    }

    fun shuffleClick() {
        _isShuffling.value = !_isShuffling.value.orFalse()
        sharedPref.setIsShuffling(_isShuffling.value.orFalse())
    }

    fun repeatClick() {
        _isRepeating.value = !_isRepeating.value.orFalse()
        sharedPref.setIsRepeating(_isRepeating.value.orFalse())
    }

    private fun getNextSongIndex(): Int {
        return if (!_isShuffling.value.orFalse())
            (_songList.value.orEmpty().indexOf(_song.value) + 1) % _songList.value.orEmpty().size
        else Random.nextInt(until = _songList.value.orEmpty().size)
    }
}