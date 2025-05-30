package com.mcvector36.sound432

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.io.File

class MusicAdapter(musicList: List<String>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    // Sortează lista melodiilor de la cel mai nou la cel mai vechi
    private val sortedMusicList = musicList.sortedByDescending { File(it).lastModified() }

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
        val songPath = sortedMusicList[position]
        val songName = songPath.substringAfterLast("/") // Extrage doar numele fișierului

        holder.songTitle.text = songName
        holder.songPath.text = songPath

        holder.cardView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = sortedMusicList.size
}
