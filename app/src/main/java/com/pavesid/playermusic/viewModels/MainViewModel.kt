package com.pavesid.playermusic.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.pavesid.playermusic.models.Song
import com.pavesid.playermusic.extensions.mutableLiveData
import com.pavesid.playermusic.repository.Repository


class MainViewModel(private var app: Application) : AndroidViewModel(app) {
    private lateinit var songs: MutableLiveData<List<Song>>

    init {
        initSongs()
    }

    private val query = mutableLiveData("")

    private fun initSongs() {
        songs = Repository(app).getSongData()
    }

    fun handleSearchQuery(text: String?) {
        query.value = text
    }

    fun getSongData(): LiveData<List<Song>> {
        val result = MediatorLiveData<List<Song>>()

        val filterF = {
            val queryStr = query.value!!
            val songItem = songs.value!!

            result.value = if (queryStr.isEmpty()) {
                songItem
            } else {
                songItem.filter { it.title.contains(queryStr, true) }
            }
        }

        result.addSource(songs) { filterF.invoke() }
        result.addSource(query) { filterF.invoke() }

        return result
    }
}