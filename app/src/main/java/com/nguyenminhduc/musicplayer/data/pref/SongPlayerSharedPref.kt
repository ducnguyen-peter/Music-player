package com.nguyenminhduc.musicplayer.data.pref

import android.content.Context
import android.content.SharedPreferences

class SongPlayerSharedPref(
    context: Context
) {

    private val pref: SharedPreferences = context.getSharedPreferences("music_player_shared_pref", Context.MODE_PRIVATE)

    fun setIsShuffling(isShuffling: Boolean) {
        pref.edit().putBoolean(IS_SHUFFLING, isShuffling).apply()
    }

    fun getIsShuffling(): Boolean {
        return pref.getBoolean(IS_SHUFFLING, false)
    }

    fun setIsRepeating(isRepeating: Boolean) {
        pref.edit().putBoolean(IS_REPEATING, isRepeating).apply()
    }

    fun getIsRepeating(): Boolean {
        return pref.getBoolean(IS_REPEATING, false)
    }

    companion object {
        const val IS_SHUFFLING = "is_shuffling"
        const val IS_REPEATING = "is_repeating"
    }
}