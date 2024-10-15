package com.nguyenminhduc.musicplayer.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nguyenminhduc.musicplayer.databinding.FragmentSongListBinding

class SongListFragment: Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }
}