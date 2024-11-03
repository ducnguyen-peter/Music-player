package com.nguyenminhduc.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nguyenminhduc.musicplayer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {

    companion object {
        const val CHANNEL_ID_1 = "channel_id_1"
        const val CHANNEL_ID_2 = "channel_id_2"
        const val ACTION_PREVIOUS = "action_previous"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PLAY = "action_play"
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(CHANNEL_ID_1, "Channel1", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel1 description"
            }
            val channel2 = NotificationChannel(CHANNEL_ID_2, "Channel2", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel2 description"
                importance = NotificationManager.IMPORTANCE_LOW
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
        }
    }
}