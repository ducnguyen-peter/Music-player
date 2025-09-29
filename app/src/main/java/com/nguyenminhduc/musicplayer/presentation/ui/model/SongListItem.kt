package com.nguyenminhduc.musicplayer.presentation.ui.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.nguyenminhduc.musicplayer.presentation.utils.getSongAlbumImage
import kotlinx.parcelize.Parcelize

sealed class SongListItem {

    @Parcelize
    data class Song(
        val path: String? = null,
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val duration: Long? = null,
        private val _albumArt: Bitmap? = null
    ) : SongListItem(), Parcelable {

        override fun equals(other: Any?): Boolean {
            return other is MusicFileUiModel &&
                    path == other.path &&
                    title == other.title &&
                    artist == other.artist &&
                    album == other.album &&
                    duration == other.duration
        }

        override fun hashCode(): Int {
            var result = path?.hashCode() ?: 0
            result = 31 * result + (title?.hashCode() ?: 0)
            result = 31 * result + (artist?.hashCode() ?: 0)
            result = 31 * result + (album?.hashCode() ?: 0)
            result = 31 * result + (duration?.hashCode() ?: 0)
            result = 31 * result + (_albumArt?.hashCode() ?: 0)
            return result
        }
    }

    data object LoadMore: SongListItem()
}