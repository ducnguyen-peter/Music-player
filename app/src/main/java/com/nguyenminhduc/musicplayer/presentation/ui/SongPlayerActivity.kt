package com.nguyenminhduc.musicplayer.presentation.ui

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nguyenminhduc.musicplayer.R
import com.nguyenminhduc.musicplayer.data.pojo.MusicFile
import com.nguyenminhduc.musicplayer.databinding.ActivitySongPlayerBinding
import com.nguyenminhduc.musicplayer.presentation.ui.mapper.MusicFileUiMapper
import com.nguyenminhduc.musicplayer.presentation.utils.toMinuteAndSecond
import kotlin.time.toDuration

class SongPlayerActivity : AppCompatActivity() {

    private var isInitialized = false

    private var _binding: ActivitySongPlayerBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val tvSongTitle get() = binding.tvSongTitle
    private val tvSongArtistName get() = binding.tvSongArtistName
    private val ivSongAlbum get() = binding.ivSongAlbum
    private val btnPlayPause get() = binding.btnPlayPause
    private val tvDuration get() = binding.tvDuration
    private val tvCurrentTime get() = binding.tvCurrentTime
    private val seekBar get() = binding.seekbar
    private val btnBack get() = binding.btnBack

    private val songInfo by lazy {
        val musicEntity = intent?.getParcelableExtra(Const.ActivityArgs.ARG_SONG_MODEL) as? MusicFile
        musicEntity?.let { MusicFileUiMapper.mapToUi(it) }
    }

    private val songList by lazy {
        val songList = intent?.getParcelableArrayListExtra<MusicFile>(Const.ActivityArgs.ARG_LIST_SONG_MODEL)
        songList?.let { song -> song.map { MusicFileUiMapper.mapToUi(it) } }
    }

    private var uri: Uri? = null
    private var mediaPlayer: MediaPlayer? = null

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
            isInitialized = true
        }
    }

    private fun setupUI() {
        btnBack.setOnClickListener { finish() }
        tvSongTitle.text = songInfo?.title
        tvSongArtistName.text = songInfo?.artist
        tvDuration.text = songInfo?.duration?.toMinuteAndSecond()
        Glide.with(this)
            .load(songInfo?.albumArt)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(ivSongAlbum)
        setupAutoPlay()
        setupSeekBar()
        setupDuration()
    }

    private fun setupAutoPlay() {
        btnPlayPause.setImageResource(R.drawable.baseline_pause_24)
        uri = Uri.parse(songInfo?.path)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        mediaPlayer?.start()
    }

    private fun setupSeekBar() {
        seekBar.max = mediaPlayer?.duration?.div(1000) ?: 0
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun setupDuration() {
        runOnUiThread {
            if (mediaPlayer != null) {
                val currentPosition = mediaPlayer?.currentPosition?.div(1000) ?: 0
                seekBar.progress = currentPosition
                tvCurrentTime.text = currentPosition.toLong().toMinuteAndSecond()
            }
        }
    }
}