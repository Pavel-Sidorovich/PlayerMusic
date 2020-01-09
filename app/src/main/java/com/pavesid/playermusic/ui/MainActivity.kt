package com.pavesid.playermusic.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.MediaController
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pavesid.playermusic.MusicController
import com.pavesid.playermusic.R
import com.pavesid.playermusic.models.Song
import com.pavesid.playermusic.service.MusicService
import com.pavesid.playermusic.ui.adapter.SongAdapter
import com.pavesid.playermusic.utils.Utils.getColorFromAttr
import com.pavesid.playermusic.viewModels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main2.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), MediaController.MediaPlayerControl {

    private val REQUEST_READ_STORAGE_PERMISSION = 1
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
        Log.d("M_Main", "0")
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("M_Main", "1")
        if(!checkPermission()) {
            requestPermission()
        } else {
            Log.d("M_Main", "12")
            initToolbar()
            Log.d("M_Main", "13")
            initViews()
            Log.d("M_Main", "14")
            initViewModel()
            Log.d("M_Main", "15")
            setController()
//            val fragmentManager = supportFragmentManager
//            val fragmentTransaction = fragmentManager.beginTransaction()
//            val fragment = MainFragment()
//            fragmentTransaction.add(R.id.fragment_container, fragment)
//            fragmentTransaction.commit()
            Log.d("M_Main", "162")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchItem.actionView as SearchView

        val searchEditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextColor(getColorFromAttr(R.attr.colorEditTextColor, theme))
        searchEditText.setHintTextColor(getColorFromAttr(R.attr.colorTextColorHint, theme))

        val searchCloseIcon =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        searchCloseIcon.setImageResource(R.drawable.ic_clear_white_24dp)

        searchView.queryHint = getString(R.string.search_song_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearchQuery(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = null
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

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.getSongData().observe(this, Observer { songAdapter.updateData(it) })
    }

    private fun checkPermission(): Boolean {
        val permissionReadStorage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionReadStorage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (shouldProvideRationale) {
            showAlertDialog(R.string.dialog_body,
                R.string.dialog_later,
                DialogInterface.OnClickListener { _, _ -> exitProcess(0) },
                R.string.dialog_ok,
                DialogInterface.OnClickListener { _, _ ->
                    ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE_PERMISSION
                )})
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_STORAGE_PERMISSION
            )
        }
    }

    private fun showAlertDialog(@StringRes mainText: Int, @StringRes actionNegative: Int, listenerNegative: DialogInterface.OnClickListener, @StringRes actionPositive: Int, listenerPositive: DialogInterface.OnClickListener) {
        val ad = AlertDialog.Builder(this)
        ad.setTitle(R.string.dialog_title)
        ad.setMessage(mainText)
        ad.setNegativeButton(
            actionNegative, listenerNegative
        )
        ad.setPositiveButton(
            actionPositive,  listenerPositive
        )
        ad.setCancelable(false)
        ad.show()
    }

    private fun showSnackBar(@StringRes mainText: Int, action: String, listener: View.OnClickListener) {
        Snackbar.make(
            findViewById(android.R.id.content),
            mainText,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(action, listener)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initToolbar()
                    initViews()
                    initViewModel()
                } else {
                    showSnackBar(R.string.dialog_body, "Ok", View.OnClickListener {
                        exitProcess(0)
                    })
                }
            }
        }
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
        Log.d("M_OnStart", "Start")
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
        Log.d("M_OnStart", "End")
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
        Log.d("M_OnDestroy", "Start")
        stopService(playIntent)
        musicService = null
        Log.d("M_OnDestroy", "End")
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

        Log.d("M_OnPause", "Start")
        super.onPause()
        paused = true
        Log.d("M_OnPause", "End")
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
