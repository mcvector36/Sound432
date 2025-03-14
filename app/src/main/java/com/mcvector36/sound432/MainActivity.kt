package com.mcvector36.sound432

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var musicList: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var playButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var prevButton: MaterialButton
    private lateinit var repeatButton: MaterialButton
    private var currentIndex = 0
    private var isRepeat = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadMusic()
            } else {
                Toast.makeText(this, "Permisiunea este necesară pentru a accesa muzica!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        repeatButton = findViewById(R.id.repeatButton)

        // Verifică și cere permisiunea de acces la media
        checkPermissions()

        // Control butoane
        playButton.setOnClickListener { togglePlayPause() }
        stopButton.setOnClickListener { stopMusic() }
        nextButton.setOnClickListener { nextTrack() }
        prevButton.setOnClickListener { previousTrack() }
        repeatButton.setOnClickListener { toggleRepeat() }
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

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MusicAdapter(musicList) { playMusic(it) }
    }

    private fun playMusic(index: Int) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        currentIndex = index
        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicList[currentIndex].substringAfter("\n")) // Extrage calea fișierului
            prepare()
            start()
        }
        playButton.setIconResource(R.drawable.play_arrow)
    }

    private fun togglePlayPause() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playButton.setIconResource(R.drawable.play_arrow)
        } else {
            mediaPlayer.start()
            playButton.setIconResource(R.drawable.play_arrow)
        }
    }

    private fun stopMusic() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        playButton.setIconResource(R.drawable.play_arrow)
    }

    private fun nextTrack() {
        if (currentIndex < musicList.size - 1) {
            playMusic(currentIndex + 1)
        } else {
            playMusic(0) // Se întoarce la primul cântec
        }
    }

    private fun previousTrack() {
        if (currentIndex > 0) {
            playMusic(currentIndex - 1)
        } else {
            playMusic(musicList.size - 1) // Mergi la ultimul cântec
        }
    }

    private fun toggleRepeat() {
        isRepeat = !isRepeat
        repeatButton.alpha = if (isRepeat) 1.0f else 0.5f // Evidențiază butonul când e activat
    }
}
