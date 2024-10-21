package com.nguyenminhduc.musicplayer.presentation.ui.mapper

import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.utils.getSongAlbumImage

object MusicFileUiMapper {

    fun mapToUi(from: MusicFile) = MusicFileUiModel(
        path = from.path,
        title = from.title,
        artist = from.artist,
        album = from.album,
        duration = from.duration,
        _albumArt = from.path.getSongAlbumImage()
    )

    fun mapToEntity(from: MusicFileUiModel) = MusicFile(
        path = from.path,
        title = from.title,
        artist = from.artist,
        album = from.album,
        duration = from.duration
    )
}