package com.nguyenminhduc.musicplayer.di

import com.nguyenminhduc.musicplayer.data.pref.SongPlayerSharedPref
import com.nguyenminhduc.musicplayer.presentation.ui.player.SongPlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        SongPlayerSharedPref(androidContext())
    }
    viewModel { parameters ->
        SongPlayerViewModel(parameters.get(), parameters.get(), get())
    }
}