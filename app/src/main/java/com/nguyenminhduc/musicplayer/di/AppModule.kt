package com.nguyenminhduc.musicplayer.di

import com.nguyenminhduc.musicplayer.presentation.ui.player.SongPlayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { parameters ->
        SongPlayerViewModel(parameters.get(), parameters.get())
    }
}