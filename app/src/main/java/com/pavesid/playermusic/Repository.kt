package com.pavesid.playermusic

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class Repository(private val application: Application) {

    private val songData = mutableLiveData(getSongs())

    companion object fun getSongData(): MutableLiveData<List<Song>> {
        Log.d("M_Rep", "${songData.value?.size}")
        return songData
    }

    private fun getSongs(): List<Song> {
        val list = mutableListOf<Song>()
        val musicResolver = application.contentResolver
        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)
        if (musicCursor != null && musicCursor.moveToFirst()) { //get columns
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
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val thisAlbum = musicCursor.getString(albumColumn)
//                    val thisPackage = musicCursor.getInt(isMusic)
                list.add(Song(thisId, thisTitle, thisArtist, thisAlbum))
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        Log.d("M_RepGet", "${list.size}")
        return list
    }

    inner class myAsyncTask : AsyncTask<Void, Void, List<Song>>() {

        override fun doInBackground(vararg params: Void): List<Song> = getSongs()

        private fun getSongs(): List<Song> {
            val list = mutableListOf<Song>()
            val musicResolver = application.contentResolver
            val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)
            if (musicCursor != null && musicCursor.moveToFirst()) { //get columns
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
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisAlbum = musicCursor.getString(albumColumn)
//                    val thisPackage = musicCursor.getInt(isMusic)
                    list.add(Song(thisId, thisTitle, thisArtist, thisAlbum))
                } while (musicCursor.moveToNext())
            }
            musicCursor?.close()
            Log.d("M_RepGet", "${list.size}")
            return list
        }
    }
}