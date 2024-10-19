package com.nguyenminhduc.musicplayer.presentation.ui.model

import android.graphics.Bitmap

data class MusicFileUiModel(
    val path: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val duration: String? = null,
    val albumArt: Bitmap? = null
)