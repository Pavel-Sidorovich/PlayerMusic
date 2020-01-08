package com.pavesid.playermusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.HeaderViewListAdapter
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.system.exitProcess
import androidx.lifecycle.ViewModelProviders


class MainActivity : AppCompatActivity(), MediaController.MediaPlayerControl {

    var songList: ArrayList<Song> = arrayListOf()
    private var musicService: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false
    private var controller: MusicController? = null
    private var paused = false
    private var playbackPaused = false

    private lateinit var songAdapter: SongAdapter
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        getSongs()

        initViews()
        initViewModel()

//        songList.sortWith(Comparator { a, b -> a.title.compareTo(b.title) })

        setController()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        Log.d("M_fd", "fdf")
//        val searchItem = menu?.findItem(R.id.app_bar_search)
////        Log.d("M_fd", "fdf")
//        val searchView = searchItem as SearchView
////        Log.d("M_fd", "fdf")
//        searchView.queryHint = "Введите имя пользователя"
//        Log.d("M_fd", "fdf")
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
////                viewModel.handleSearchQuery(query)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
////                viewModel.handleSearchQuery(newText)
//                return true
//            }
//        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun initViews() {

        songAdapter = SongAdapter {
            musicService!!.playSong(it)
        }

        songAdapter.updateData(songList)

        with(song_list) {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun initViewModel() {
        Log.d("M_MainActivity", "initModel")

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        Log.d("M_MainActivity", "initModel2")
        viewModel.getSongData().observe(this, Observer { songAdapter.updateData(it) })
    }

//    private fun getSongs() {
//        val musicResolver = contentResolver
//        val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)
//        if (musicCursor != null && musicCursor.moveToFirst()) { //get columns
//            val titleColumn =
//                musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
//            val idColumn =
//                musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
//            val artistColumn =
//                musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
//            val albumColumn =
//                musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
//            val isMusic =
//                musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
//            //add songs to list
//            do {
//                val thisId = musicCursor.getLong(idColumn)
//                val thisTitle = musicCursor.getString(titleColumn)
//                val thisArtist = musicCursor.getString(artistColumn)
//                val thisAlbum = musicCursor.getString(albumColumn)
//                val thisPackage = musicCursor.getInt(isMusic)
//                songList.add(Song(thisId, thisTitle, thisArtist, thisAlbum, thisPackage.toString()))
//            } while (musicCursor.moveToNext())
//        }
//        musicCursor?.close()
//    }

    private val musicConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            musicBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.service
            musicService!!.setList(songList)
            musicBound = true
        }

    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_end -> {
                stopService(playIntent)
                musicService = null
                exitProcess(0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicService = null
        super.onDestroy()
    }

    fun songPicked(view: View) {
        musicService!!.setSong(Integer.parseInt(view.tag.toString()))
        musicService!!.playSong()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller!!.show(0)
    }

    private fun setController() {
        controller = MusicController(this)

        controller!!.setPrevNextListeners(
            { playNext() },
            { playPrev() })

        controller!!.setMediaPlayer(this)
        controller!!.setAnchorView(song_list)
        controller!!.isEnabled = true
    }

    private fun playNext() {
        musicService!!.playNext()
        if(playbackPaused) {
            setController()
            playbackPaused=false
        }
        controller!!.show(0)
    }

    private fun playPrev() {
        musicService!!.playPrev()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller!!.show(0)
    }

    override fun isPlaying(): Boolean =
        if (musicService != null && musicBound) {
            musicService!!.isPng()
        } else false

    override fun canSeekForward(): Boolean = true

    override fun getDuration(): Int =
        if (musicService != null && musicBound && musicService!!.isPng()) {
            musicService!!.getDur()
        } else 0

    override fun pause() {
        playbackPaused = true
        musicService!!.pausePlayer()
    }

    override fun getBufferPercentage(): Int = 0

    override fun seekTo(pos: Int) {
        musicService!!.seek(pos)
    }

    override fun getCurrentPosition(): Int =
        if (musicService != null && musicBound && musicService!!.isPng()) {
            musicService!!.getPos()
        } else 0

    override fun canSeekBackward(): Boolean = true

    override fun start() {
        musicService!!.go()
    }

    override fun getAudioSessionId(): Int = 0

    override fun canPause(): Boolean = true

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if(paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller!!.hide()
        super.onStop()
    }
}
