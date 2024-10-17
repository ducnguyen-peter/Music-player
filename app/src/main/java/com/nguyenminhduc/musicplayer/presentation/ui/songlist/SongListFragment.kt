package com.nguyenminhduc.musicplayer.presentation.ui.songlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nguyenminhduc.musicplayer.databinding.FragmentSongListBinding

class SongListFragment: Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val rvSongList get() = binding.rvSongList

    private val viewModel: SongListViewModel by activityViewModels()

    private val adapter = SongListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupViewModel()
    }

    private fun setupUI() {
        rvSongList.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel.songList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}