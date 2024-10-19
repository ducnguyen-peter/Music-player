package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.nguyenminhduc.musicplayer.R
import com.nguyenminhduc.musicplayer.databinding.ItemRowSongBinding
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel

class SongListAdapter : ListAdapter<MusicFileUiModel, SongListAdapter.SongItemViewHolder>(
    object : DiffUtil.ItemCallback<MusicFileUiModel>() {
        override fun areItemsTheSame(oldItem: MusicFileUiModel, newItem: MusicFileUiModel): Boolean {
            return oldItem.path == newItem.path && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: MusicFileUiModel, newItem: MusicFileUiModel): Boolean {
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
    ) : ViewHolder(binding.root) {

        private val ivThumbnail get() = binding.ivThumbnail
        private val tvSongTitle get() = binding.tvSongTitle
        private val tvArtist get() = binding.tvArtist

        fun bind(song: MusicFileUiModel) {
            tvSongTitle.text = song.title
            tvArtist.text = song.artist
            Glide.with(itemView.context)
                .load(song.albumArt)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivThumbnail)
        }
    }
}