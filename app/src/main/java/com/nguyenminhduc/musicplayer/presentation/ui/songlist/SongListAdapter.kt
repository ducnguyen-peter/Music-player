package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nguyenminhduc.musicplayer.R
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.databinding.ItemRowSongBinding

class SongListAdapter: ListAdapter<MusicFile, SongListAdapter.SongItemViewHolder>(
    object : DiffUtil.ItemCallback<MusicFile>() {
        override fun areItemsTheSame(oldItem: MusicFile, newItem: MusicFile): Boolean {
            return oldItem.path == newItem.path && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: MusicFile, newItem: MusicFile): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongItemViewHolder {
        return SongItemViewHolder(
            ItemRowSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SongItemViewHolder(
        private val binding: ItemRowSongBinding
    ): ViewHolder(binding.root) {

        private val ivThumbnail get() = binding.ivThumbnail
        private val tvSongTitle get() = binding.tvSongTitle
        private val tvArtist get() = binding.tvArtist

        fun bind(song: MusicFile) {
            tvSongTitle.text = song.title
            tvArtist.text = song.artist
            Runnable {
                val thumbNailBytes = getSongAlbumImage(song.path)
                val bitmap = thumbNailBytes.takeIf { it.isNotEmpty() }?.let {
                    BitmapFactory.decodeByteArray(
                        thumbNailBytes,
                        0,
                        thumbNailBytes.size
                    )
                }
                bitmap?.let {
                    ivThumbnail.setImageBitmap(
                        Bitmap.createScaledBitmap(
                            it,
                            30,
                            30,
                            false
                        )
                    )
                    it.recycle()
                } ?: ivThumbnail.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_launcher_foreground))
            }.run()
        }

        private fun getSongAlbumImage(uri: String?): ByteArray {
            val retriever = MediaMetadataRetriever()
            return try { retriever.setDataSource(uri)
                val imageByte = retriever.embeddedPicture
                retriever.close()
                imageByte ?: ByteArray(0)
            } catch (e: Exception) {
                ByteArray(0)
            }
        }
    }
}