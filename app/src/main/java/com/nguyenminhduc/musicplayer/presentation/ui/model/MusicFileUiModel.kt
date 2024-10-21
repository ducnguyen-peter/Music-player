package com.nguyenminhduc.musicplayer.presentation.ui.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.nguyenminhduc.musicplayer.presentation.utils.getSongAlbumImage
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicFileUiModel(
    val path: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long? = null,
    private val _albumArt: Bitmap? = null
) : Parcelable {

    val albumArt get() = try {
        _albumArt
    } catch (e: Exception) {
        path.getSongAlbumImage()
    }
}