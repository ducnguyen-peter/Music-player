package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.domain.usecase.GetLocalMusicFilesUseCase
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.ui.model.PagingState
import com.nguyenminhduc.musicplayer.presentation.utils.getSongAlbumImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SongListViewModel(
    private val getLocalMusicFilesUseCase: GetLocalMusicFilesUseCase
) : ViewModel() {

    private val _songList = MutableLiveData<List<MusicFileUiModel>>()
    val songList: LiveData<List<MusicFileUiModel>> = _songList

    private val pagingState: PagingState = PagingState()

    fun fetchSongs(context: Context) {
        viewModelScope.launch {
            getLocalMusicFilesUseCase.invoke(
                GetLocalMusicFilesUseCase.Params(
                    context.contentResolver,
                    pagingState
                )
            )
                .flowOn(Dispatchers.IO)
                .collect {
                    _songList.postValue(it)
                }
        }
    }
}