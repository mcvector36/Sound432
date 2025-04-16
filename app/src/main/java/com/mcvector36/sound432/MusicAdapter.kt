package com.mcvector36.sound432

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.io.File

class MusicAdapter(
    private var musicList: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var currentPlayingIndex: Int = -1
    private val sortedMusicList = musicList.sortedByDescending { File(it.substringAfter("\n")).lastModified() }

    class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val songPath: TextView = view.findViewById(R.id.songPath)
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val songInfo = sortedMusicList[position]
        val songName = songInfo.substringBefore("\n")
        val songPath = songInfo.substringAfter("\n")

        holder.songTitle.text = songName
        holder.songPath.text = songPath

        // 🔹 Evidențiază piesa care se redă
        if (position == currentPlayingIndex) {
            holder.cardView.setStrokeColor(Color.parseColor("#03DAC5")) // culoare turcoaz
            holder.cardView.strokeWidth = 6
        } else {
            holder.cardView.setStrokeColor(Color.TRANSPARENT)
            holder.cardView.strokeWidth = 0
        }

        holder.cardView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = sortedMusicList.size

    fun setCurrentPlaying(index: Int) {
        val previousIndex = currentPlayingIndex
        currentPlayingIndex = index
        if (previousIndex != -1) notifyItemChanged(previousIndex)
        notifyItemChanged(currentPlayingIndex)
    }
}
