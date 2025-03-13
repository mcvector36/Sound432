package com.mcvector36.sound432

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var musicList: ArrayList<String>
    private lateinit var listView: RecyclerView
    private lateinit var playButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var prevButton: MaterialButton
    private lateinit var repeatButton: MaterialButton
    private var currentIndex = 0
    private var isRepeat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.recyclerView)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        repeatButton = findViewById(R.id.repeatButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            loadMusic()
        }
    }

    private fun loadMusic() {
        musicList = ArrayList()
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = musicResolver.query(musicUri, null, null, null, null)

        cursor?.let {
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                val path = it.getString(titleColumn)
                musicList.add(path)
            }
            it.close()
        }

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = MusicAdapter(musicList) { playMusic(it) }
    }

    private fun playMusic(index: Int) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        currentIndex = index
        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicList[currentIndex])
            prepare()
            start()
        }
    }
}
