package com.nguyenminhduc.musicplayer.presentation.ui.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_NEXT
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PLAY
import com.nguyenminhduc.musicplayer.MainApplication.Companion.ACTION_PREVIOUS
import com.nguyenminhduc.musicplayer.MainApplication.Companion.CHANNEL_ID_2
import com.nguyenminhduc.musicplayer.R
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.databinding.ActivitySongPlayerBinding
import com.nguyenminhduc.musicplayer.presentation.receiver.NotificationReceiver
import com.nguyenminhduc.musicplayer.presentation.service.SongPlayingService
import com.nguyenminhduc.musicplayer.presentation.ui.Const
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.utils.toMinuteAndSecond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class SongPlayerActivity : AppCompatActivity(), ServiceConnection, PlayerController {

    private var isInitialized = false

    private var _binding: ActivitySongPlayerBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val main get() = binding.main
    private val tvSongTitle get() = binding.tvSongTitle
    private val tvSongArtistName get() = binding.tvSongArtistName
    private val ivSongAlbum get() = binding.ivSongAlbum
    private val btnPlayPause get() = binding.btnPlayPause
    private val btnNext get() = binding.btnNext
    private val btnPrevious get() = binding.btnPrevious
    private val btnShuffle get() = binding.btnShuffle
    private val btnLoop get() = binding.btnLoop
    private val tvDuration get() = binding.tvDuration
    private val tvCurrentTime get() = binding.tvCurrentTime
    private val seekBar get() = binding.seekbar
    private val btnBack get() = binding.btnBack

    private val viewModel: SongPlayerViewModel by viewModel {
        parametersOf(
            intent?.getIntExtra(Const.ActivityArgs.ARG_SONG_INDEX, 0),
            (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL))?.let { song ->
                song.map { MusicFileUiMapper.mapToUi(it) }
            }
        )
    }

    private var currentSong: MusicFileUiModel = MusicFileUiModel()
    private var countUpJob: Job? = null
    private var lastDuration: Long = 0
    private var songPlayingService: SongPlayingService? = null
    private var mediaSession: MediaSession? = null
    private val notificationReceiver = NotificationReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivitySongPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupReceiver()
    }

    override fun onStart() {
        super.onStart()
        if (!isInitialized) {
            setupUI()
            setupViewModel()
            isInitialized = true
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, SongPlayingService::class.java).putParcelableArrayListExtra(
            Const.ActivityArgs.ARG_LIST_SONG_MODEL, (intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL)) as ArrayList
        )
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setupReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_PLAY)
        intentFilter.addAction(ACTION_NEXT)
        intentFilter.addAction(ACTION_PREVIOUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                notificationReceiver,
                intentFilter,
                RECEIVER_NOT_EXPORTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(notificationReceiver, intentFilter)
        } else {
            registerReceiver(notificationReceiver, intentFilter)
        }
    }

    private fun setupUI() {
        btnBack.setOnClickListener {
            finish()
            songPlayingService?.stop()
            songPlayingService?.release()
        }
        setupSeekBar()
        setupBtnPlayPause()
        setupBtnNext()
        setupBtnPrevious()
        setupBtnShuffle()
        setupBtnLoop()
    }

    private fun setupViewModel() {
        viewModel.song.observe(this) {
            updateUI(it)
        }
        viewModel.isShuffling.observe(this) {
            shufflePlayList(it)
        }
        viewModel.isRepeating.observe(this) {
            repeatSong(it)
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    songPlayingService?.seekTo(progress * 1000)
                    setupDuration(start = (progress * 1000).toLong(), end = viewModel.song.value?.duration ?: 0L)
                }
                if (progress == p0?.max && viewModel.song.value?.path?.endsWith(".ogg") == true) { // custom completion listener for .ogg
                    viewModel.updateSong(viewModel.getNextSong()?.also { setupAutoPlay(it) })
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun setupBtnPlayPause() {
        btnPlayPause.setOnClickListener {
            onPlayPauseClick()
        }
    }

    private fun setupBtnNext() {
        btnNext.setOnClickListener {
            onNextClick()
        }
    }

    private fun setupBtnPrevious() {
        btnPrevious.setOnClickListener {
            onPreviousClick()
        }
    }

    private fun setupBtnShuffle() {
        btnShuffle.setOnClickListener {
            viewModel.shuffleClick()
        }
    }

    private fun setupBtnLoop() {
        btnLoop.setOnClickListener {
            viewModel.repeatClick()
        }
    }

    private fun updateUI(song: MusicFileUiModel) {
        tvSongTitle.text = song.title
        tvSongArtistName.text = song.artist
        tvDuration.text = song.duration?.toMinuteAndSecond()
        CoroutineScope(Dispatchers.Main).launch {
            Glide.with(this@SongPlayerActivity)
                .load(song.albumArt.await())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivSongAlbum)
        }
        mediaSession = MediaSession(this, "My Media Session")
        mediaSession?.setMetadata(
            MediaMetadata.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration ?: 0L)
                .build()
        )
    }

    private fun setupAutoPlay(song: MusicFileUiModel) {
        if (song == currentSong) return
        currentSong = song
        btnPlayPause.setImageResource(R.drawable.baseline_pause_24)
        songPlayingService?.stop()
        songPlayingService?.release()
        songPlayingService?.createMediaPlayer(viewModel.songList.value.orEmpty().indexOf(song))
        songPlayingService?.start()
        songPlayingService?.setOnCompletionListener {
            viewModel.updateSong(viewModel.getNextSong()?.also { setupAutoPlay(it) })
        }
        updateSeekBarDuration(song)
        setupDuration(end = song.duration ?: 0L)
        lifecycleScope.launch { showNotification(song) }
    }


    private fun setupDuration(start: Long = 0, end: Long = 0) {
        stopCountUp()
        if (start >= end) return
        countUpJob = lifecycleScope.launch {
            countUp(start = start, expireTime = end)
                .flowOn(Dispatchers.IO)
                .collect {
                    lastDuration = it
                    seekBar.progress = (it / 1000).toInt()
                    tvCurrentTime.text = it.toMinuteAndSecond()
                }
        }
    }

    private fun updateSeekBarDuration(song: MusicFileUiModel) {
        seekBar.max = song.duration?.toInt()?.div(1000) ?: 0
    }

    private fun shufflePlayList(isShuffle: Boolean) {
        btnShuffle.setImageResource(
            if (isShuffle) R.drawable.baseline_shuffle_on_24 else R.drawable.baseline_shuffle_24
        )
    }

    private fun repeatSong(isRepeating: Boolean) {
        btnLoop.setImageResource(
            if (isRepeating) R.drawable.baseline_repeat_on_24 else R.drawable.baseline_repeat_off_24
        )
    }

    private fun countUp(
        expireTime: Long,
        period: Long = 1000,
        start: Long = 0,
        initialDelay: Long = 0
    ) = flow {
        delay(initialDelay)

        var counter = start
        while (counter < expireTime) {
            emit(counter)
            counter += period

            delay(period)
        }

        emit(expireTime)
    }

    private fun stopCountUp() {
        countUpJob.takeIf { it?.isActive == true }?.cancel()
        countUpJob = null
    }

    private suspend fun showNotification(song: MusicFileUiModel) {
        val playPauseIcon =
            if (songPlayingService?.isPlaying() == true) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
        val intent = Intent(this, SongPlayerActivity::class.java)
        val prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREVIOUS)
        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)
        val playPauseIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY)

        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val playPausePending = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(song.albumArt.await())
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .addAction(Notification.Action.Builder(Icon.createWithResource(this, R.drawable.baseline_skip_previous_24), "Previous", prevPending).build())
                .addAction(Notification.Action.Builder(Icon.createWithResource(this, playPauseIcon), "Pause", playPausePending).build())
                .addAction(Notification.Action.Builder(Icon.createWithResource(this, R.drawable.baseline_skip_next_24), "Next", nextPending).build())
                .setStyle(Notification.MediaStyle().setMediaSession(mediaSession?.sessionToken))
        else {
            Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(song.albumArt.await())
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(playPauseIcon, "Pause", playPausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
                .setStyle(Notification.MediaStyle().setMediaSession(mediaSession?.sessionToken))
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(longArrayOf(0L))
        }.build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        songPlayingService = (service as? SongPlayingService.SongPlayingServiceBinder)?.getService()
        songPlayingService?.playerController = this
        viewModel.song.value?.let { setupAutoPlay(it) }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        songPlayingService = null
    }

    override fun onPlayPauseClick() {
        if (songPlayingService?.isPlaying() == true) {
            btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24)
            TransitionManager.beginDelayedTransition(main)
            songPlayingService?.pause()
            stopCountUp()
        } else {
            btnPlayPause.setImageResource(R.drawable.baseline_pause_24)
            TransitionManager.beginDelayedTransition(main)
            songPlayingService?.start()
            setupDuration(start = lastDuration, end = viewModel.song.value?.duration ?: 0L)
        }
        lifecycleScope.launch { showNotification(currentSong) }
    }

    override fun onNextClick() {
        val nextSong = viewModel.getNextSong()?.also { setupAutoPlay(it) }
        viewModel.updateSong(nextSong)
    }

    override fun onPreviousClick() {
        val prevSong = viewModel.getPrevSong()?.also { setupAutoPlay(it) }
        viewModel.updateSong(prevSong)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
        unregisterReceiver(notificationReceiver)
    }
}