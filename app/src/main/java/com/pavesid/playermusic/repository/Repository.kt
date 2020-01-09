package com.pavesid.playermusic.repository

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.pavesid.playermusic.models.Song
import com.pavesid.playermusic.extensions.mutableLiveData

class Repository(private val application: Application) {

    private val songData =
        mutableLiveData(getSongs())

    companion object

    fun getSongData(): MutableLiveData<List<Song>> {
        Log.d("M_Rep", "${songData.value?.size}")
        return songData
    }

    private fun getSongs(): List<Song> {
        Log.d("M_GETTER", "hjh")
        val list = mutableListOf<Song>()
        val musicResolver = application.contentResolver
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn =
                musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn =
                musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn =
                musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn =
                musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val isMusic =
                musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
            //add songs to list
            do {
                if (musicCursor.getString(isMusic) != null) {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisAlbum = musicCursor.getString(albumColumn)
                    list.add(
                        Song(
                            thisId,
                            thisTitle,
                            thisArtist,
                            thisAlbum
                        )
                    )
                }
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        return list
    }
}