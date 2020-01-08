package com.pavesid.playermusic

import android.app.Application
import android.util.Log
import androidx.lifecycle.*


class MainViewModel(var app: Application) : AndroidViewModel(app) {
    private lateinit var songs: MutableLiveData<List<Song>>
    init {

        initSongs()
        Log.d("M_fd", "${songs.value?.size}")
    }

    private val query = mutableLiveData("")
//    private var songs = Repository(app).getSongData()

    fun initSongs() {
        songs = Repository(app).getSongData()
    }


    fun handleSearchQuery(text: String?) {
        query.value = text
    }

    fun getSongData(): LiveData<List<Song>> {
        val result = MediatorLiveData<List<Song>>()

        Log.d("M_View", "fdf")

        val filterF = {
            val queryStr = query.value!!
            val chatItem = songs.value!!

            result.value = if (queryStr.isEmpty()) {
                chatItem
            } else {
                chatItem.filter { it.title.contains(queryStr, true) }
            }
        }

        result.addSource(songs) { filterF.invoke() }
        result.addSource(query) { filterF.invoke() }

        return result
    }
}