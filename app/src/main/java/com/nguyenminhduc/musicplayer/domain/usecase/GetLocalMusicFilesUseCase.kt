package com.nguyenminhduc.musicplayer.domain.usecase

import android.content.ContentResolver
import com.nguyenminhduc.musicplayer.data.query.MusicFileQueryService
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.ui.model.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetLocalMusicFilesUseCase(private val service: MusicFileQueryService) {

    data class Params(
        val contentResolver: ContentResolver,
        val pagingState: PagingState
    )

    fun invoke(params: Params): Flow<List<MusicFileUiModel>> {
         return service.getLocalMusicFiles(
             params.contentResolver,
             params.pagingState
         ).map { musicFiles ->
             musicFiles.map {
                 MusicFileUiMapper.mapToUi(it)
             }
         }
    }
}