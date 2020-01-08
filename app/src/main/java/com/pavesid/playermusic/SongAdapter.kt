package com.pavesid.playermusic

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song.view.*


class SongAdapter(private val listener: (Song)-> Unit) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var items: List<Song> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SongViewHolder(inflater.inflate(R.layout.song, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    fun updateData(data : List<Song>){

        val diffCallback = object : DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = items[oldItemPosition].id == data[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = items[oldItemPosition].hashCode() == data[newItemPosition].hashCode()

            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = data.size

        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        Log.d("M_update", "${items.size}")

        items = data
        Log.d("M_update", "${items.size}")
        diffResult.dispatchUpdatesTo(this)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(song: Song, listener: (Song)-> Unit) {
            itemView.song_title.text = song.title
            itemView.song_album.text = song.album
            itemView.song_artist.text = song.artist
            itemView.setOnClickListener { listener.invoke(song) }
        }

    }
}