package com.pavesid.playermusic.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pavesid.playermusic.R
import com.pavesid.playermusic.models.Song
import com.pavesid.playermusic.utils.Utils.getColorFromAttr
import kotlinx.android.synthetic.main.item_song_single.view.*
import kotlinx.android.synthetic.main.song.view.*


class SongAdapter(private val listener: (Song)-> Unit) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var items: List<Song> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        Log.d("M_ViewHolder", "${items.size}")
        val inflater = LayoutInflater.from(parent.context)
        Log.d("M_ViewHolder", "2 - ${items.size}")
        return SongViewHolder(inflater.inflate(R.layout.item_song_single, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        Log.d("M_ViewHolder", "${items[position]}")
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

        items = data
        diffResult.dispatchUpdatesTo(this)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init{
            Log.d("M_InViewH", "ff")
        }
        fun bind(song: Song, listener: (Song)-> Unit) {
            Log.d("M_OnBind", "start")
            itemView.tv_title_song.text = song.title
            itemView.tv_author_song.text = "${song.artist}|${song.album}"
            itemView.iv_image_song.setBackgroundColor(getColorFromAttr(R.attr.colorBackground, itemView.context.theme))
            itemView.iv_image_song.setImageResource(R.drawable.ic_music_note_black_24dp)
            itemView.setOnClickListener { listener.invoke(song) }
            Log.d("M_OnBind", "end")
        }
    }
}