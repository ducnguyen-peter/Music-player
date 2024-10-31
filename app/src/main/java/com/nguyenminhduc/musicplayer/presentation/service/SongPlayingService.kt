package com.nguyenminhduc.musicplayer.presentation.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.Const
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.utils.orFalse

class SongPlayingService: Service() {

    private val binder = SongPlayingServiceBinder()
    var mediaPlayer: MediaPlayer? = null
    var songList = listOf<MusicFile>()
    var uri: Uri? = null

    override fun onBind(intent: Intent?): IBinder? {
        songList = (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL)).orEmpty()
        return binder
    }

    inner class SongPlayingServiceBinder: Binder() {
        fun getService(): SongPlayingService = this@SongPlayingService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        songList = (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL)).orEmpty()
        return super.onStartCommand(intent, flags, startId)
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying.orFalse()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun release() {
        mediaPlayer?.release()
    }

    fun getDuration() {
        mediaPlayer?.duration
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun createMediaPlayer(index: Int) {
        uri = Uri.parse(songList[index].path)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun setOnCompletionListener(function: () -> Unit) {
        mediaPlayer?.setOnCompletionListener { function.invoke() }
    }
}