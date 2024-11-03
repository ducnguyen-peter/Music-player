package com.nguyenminhduc.musicplayer.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_NEXT
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PLAY
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PREVIOUS
import com.nguyenminhduc.musicplayer.presentation.service.SongPlayingService

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, SongPlayingService::class.java)
        when(val actionName = intent?.action) {
            ACTION_PLAY, ACTION_NEXT, ACTION_PREVIOUS -> {
                serviceIntent.putExtra("ActionName", actionName)
                context?.startService(serviceIntent)
            }
            else -> {}
        }
    }
}