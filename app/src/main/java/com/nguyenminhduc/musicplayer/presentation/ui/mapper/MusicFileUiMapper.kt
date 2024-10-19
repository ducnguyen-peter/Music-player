package com.nguyenminhduc.musicplayer.presentation.ui.mapper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel

object MusicFileUiMapper {

    fun map(from: MusicFile) = MusicFileUiModel(
        path = from.path,
        title = from.title,
        artist = from.artist,
        album = from.album,
        duration = from.duration,
        albumArt = getSongAlbumImage(from.path)
    )

    private fun getSongAlbumImage(uri: String?): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
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