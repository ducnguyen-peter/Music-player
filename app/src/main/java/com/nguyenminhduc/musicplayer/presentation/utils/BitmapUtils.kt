package com.nguyenminhduc.musicplayer.presentation.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

fun String?.getSongAlbumImage(): Deferred<Bitmap?> {
    val retriever = MediaMetadataRetriever()
    return CoroutineScope(Dispatchers.IO).async {
        try {
            retriever.setDataSource(this@getSongAlbumImage)
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
}