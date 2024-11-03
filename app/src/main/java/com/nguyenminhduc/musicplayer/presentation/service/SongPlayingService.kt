package com.nguyenminhduc.musicplayer.presentation.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_NEXT
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PLAY
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PREVIOUS
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.presentation.ui.Const
import com.nguyenminhduc.musicplayer.presentation.ui.player.PlayerController
import com.nguyenminhduc.musicplayer.presentation.utils.orFalse

class SongPlayingService: Service() {

    private val binder = SongPlayingServiceBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var songList = listOf<MusicFile>()
//    private var uri: Uri? = null
    var playerController: PlayerController? = null

    override fun onBind(intent: Intent?): IBinder? {
        songList = (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL)).orEmpty()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer = null
        return super.onUnbind(intent)
    }

    inner class SongPlayingServiceBinder: Binder() {
        fun getService(): SongPlayingService = this@SongPlayingService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL))?.let { songList = it }
        when (intent?.getStringExtra("ActionName")) {
            ACTION_PLAY -> playerController?.onPlayPauseClick()
            ACTION_NEXT -> playerController?.onNextClick()
            ACTION_PREVIOUS -> playerController?.onPreviousClick()
        }
        return START_STICKY
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
        val uri = Uri.parse(songList[index].path)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun setOnCompletionListener(function: () -> Unit) {
        mediaPlayer?.setOnCompletionListener { function.invoke() }
    }
}