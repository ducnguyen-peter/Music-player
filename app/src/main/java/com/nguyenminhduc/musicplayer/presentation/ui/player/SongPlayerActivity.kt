package com.nguyenminhduc.musicplayer.presentation.ui.player

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.net.Uri
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
import com.nguyenminhduc.musicplayer.R
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.data.pref.SongPlayerSharedPref
import com.nguyenminhduc.musicplayer.databinding.ActivitySongPlayerBinding
import com.nguyenminhduc.musicplayer.presentation.service.SongPlayingService
import com.nguyenminhduc.musicplayer.presentation.ui.Const
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.ui.model.MusicFileUiModel
import com.nguyenminhduc.musicplayer.presentation.utils.toMinuteAndSecond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SongPlayerActivity : AppCompatActivity(), ServiceConnection {

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

    private var uri: Uri? = null
//    private var mediaPlayer: MediaPlayer? = null
    private var countUpJob: Job? = null
    private var lastDuration: Long = 0
    private var songPlayingService: SongPlayingService? = null

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
                    viewModel.onAutoNext()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun setupBtnPlayPause() {
        btnPlayPause.setOnClickListener {
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
        }
    }

    private fun setupBtnNext() {
        btnNext.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    private fun setupBtnPrevious() {
        btnPrevious.setOnClickListener {
            viewModel.onPreviousClick()
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
        Glide.with(this)
            .load(song.albumArt)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(ivSongAlbum)
        setupAutoPlay(song)
        updateSeekBarDuration(song)
        setupDuration(end = song.duration ?: 0L)
    }

    private fun setupAutoPlay(song: MusicFileUiModel) {
        btnPlayPause.setImageResource(R.drawable.baseline_pause_24)
        songPlayingService?.stop()
        songPlayingService?.release()
        songPlayingService?.createMediaPlayer(viewModel.songList.value.orEmpty().indexOf(song))
        songPlayingService?.start()
        songPlayingService?.setOnCompletionListener {
            viewModel.onAutoNext()
        }
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

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        songPlayingService = (service as? SongPlayingService.SongPlayingServiceBinder)?.getService()
        viewModel.song.value?.let { setupAutoPlay(it) }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        songPlayingService = null
    }
}