package com.nguyenminhduc.musicplayer.presentation.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

fun String?.getSongAlbumImage(): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this)
        val imageByte = retriever.embeddedPicture
        retriever.close()
        imageByte?.takeIf { it.isNotEmpty() }?.let {
            BitmapFactory.decodeByteArray(
                imageByte,
                0,
                imageByte.size
            )
        }
    } catch (e: Exception) {
        null
    }
}