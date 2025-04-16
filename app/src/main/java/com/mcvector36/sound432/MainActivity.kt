package com.mcvector36.sound432

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.io.File
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var musicList: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var playButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var prevButton: MaterialButton
    private lateinit var repeatButton: MaterialButton
    private lateinit var detuneButton: MaterialButton
    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private var seekBarHandler = Handler(Looper.getMainLooper())
    private lateinit var seekBarRunnable: Runnable

    private var currentIndex = 0
    private var isRepeat = false
    private var isDetuned = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadMusic()
            } else {
                Toast.makeText(this, "Permisiunea este necesară pentru a accesa muzica!", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        repeatButton = findViewById(R.id.repeatButton)
        detuneButton = findViewById(R.id.detuneButton)
        seekBar = findViewById(R.id.seekBar)
        currentTimeText = findViewById(R.id.currentTimeText)
        totalTimeText = findViewById(R.id.totalTimeText)

        prevButton.setIconResource(R.drawable.skip_previous)
        playButton.setIconResource(R.drawable.play_arrow)
        stopButton.setIconResource(R.drawable.stop)
        nextButton.setIconResource(R.drawable.skip_next)
        repeatButton.setIconResource(R.drawable.repeat)
        detuneButton.setIconResource(R.drawable.tune432)

        checkPermissions()

        playButton.setOnClickListener { togglePlayPause() }
        stopButton.setOnClickListener { stopMusic() }
        nextButton.setOnClickListener { nextTrack() }
        prevButton.setOnClickListener { previousTrack() }
        repeatButton.setOnClickListener { toggleRepeat() }
        detuneButton.setOnClickListener { detuneMusic() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)
                    currentTimeText.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else {
            loadMusic()
        }
    }

    private fun loadMusic() {
        musicList = ArrayList()
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor = musicResolver.query(musicUri, projection, selection, null, null)

        cursor?.use {
            val pathColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val nameColumn = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                val name = it.getString(nameColumn)
                musicList.add("$name\n$path")
            }
        }

        if (musicList.isEmpty()) {
            Toast.makeText(this, "Nu s-au găsit fișiere audio!", Toast.LENGTH_LONG).show()
        }

        musicList.sortByDescending {
            val file = File(it.substringAfter("\n"))
            if (file.exists()) file.lastModified() else 0
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MusicAdapter(musicList) { playMusic(it) }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun playMusic(index: Int) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        currentIndex = index
        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicList[currentIndex].substringAfter("\n"))
            prepare()
            start()

            setOnCompletionListener {
                if (isRepeat) {
                    playMusic(currentIndex)
                } else {
                    nextTrack()
                }
            }
        }

        applyDetune()

        seekBar.max = mediaPlayer.duration
        totalTimeText.text = formatTime(mediaPlayer.duration)
        currentTimeText.text = "00:00"

        seekBarRunnable = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    val pos = mediaPlayer.currentPosition
                    seekBar.progress = pos
                    currentTimeText.text = formatTime(pos)
                    seekBarHandler.postDelayed(this, 500)
                }
            }
        }
        seekBarHandler.post(seekBarRunnable)

        playButton.setIconResource(R.drawable.pause)
    }

    private fun togglePlayPause() {
        if (musicList.isEmpty()) {
            Toast.makeText(this, "Nu există melodii disponibile!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::mediaPlayer.isInitialized) {
            playMusic(0)
        } else if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playButton.setIconResource(R.drawable.play_arrow)
        } else {
            mediaPlayer.start()
            playButton.setIconResource(R.drawable.pause)
        }
    }

    private fun stopMusic() {
        if (musicList.isEmpty()) return

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
            seekBarHandler.removeCallbacks(seekBarRunnable)
        }

        seekBar.progress = 0
        currentTimeText.text = "00:00"
        totalTimeText.text = "00:00"
        playButton.setIconResource(R.drawable.play_arrow)
    }

    private fun nextTrack() {
        if (currentIndex < musicList.size - 1) {
            playMusic(currentIndex + 1)
        } else {
            playMusic(0)
        }
    }

    private fun previousTrack() {
        if (currentIndex > 0) {
            playMusic(currentIndex - 1)
        } else {
            playMusic(musicList.size - 1)
        }
    }

    private fun toggleRepeat() {
        if (musicList.isEmpty()) return
        isRepeat = !isRepeat
        mediaPlayer.isLooping = isRepeat
        repeatButton.alpha = if (isRepeat) 0.5f else 1.0f
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun detuneMusic() {
        if (musicList.isEmpty()) return

        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            val playbackParams = mediaPlayer.playbackParams
            isDetuned = !isDetuned

            if (isDetuned) {
                playbackParams.pitch = 0.981f
                val volumeBoost = 10.0.pow(6.0 / 20.0).toFloat()
                mediaPlayer.setVolume(volumeBoost, volumeBoost)
            } else {
                playbackParams.pitch = 1.0f
                mediaPlayer.setVolume(1.0f, 1.0f)
            }

            playbackParams.speed = 1.0f
            mediaPlayer.playbackParams = playbackParams

            detuneButton.alpha = if (isDetuned) 0.5f else 1.0f
            detuneButton.invalidate()
        } else {
            Toast.makeText(this, "Nicio melodie nu este redată!", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun applyDetune() {
        if (::mediaPlayer.isInitialized) {
            val playbackParams = mediaPlayer.playbackParams

            if (isDetuned) {
                playbackParams.pitch = 0.981f
                val volumeBoost = 10.0.pow(6.0 / 20.0).toFloat()
                mediaPlayer.setVolume(volumeBoost, volumeBoost)
            } else {
                playbackParams.pitch = 1.0f
                mediaPlayer.setVolume(1.0f, 1.0f)
            }

            playbackParams.speed = 1.0f
            mediaPlayer.playbackParams = playbackParams
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seekBarHandler.removeCallbacks(seekBarRunnable)
    }

    private fun formatTime(ms: Int): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
