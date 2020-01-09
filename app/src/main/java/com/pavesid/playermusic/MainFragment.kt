package com.pavesid.playermusic

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.MediaController
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.pavesid.playermusic.models.Song
import com.pavesid.playermusic.service.MusicService
import com.pavesid.playermusic.ui.adapter.SongAdapter
import com.pavesid.playermusic.utils.Utils
import com.pavesid.playermusic.viewModels.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlin.system.exitProcess

class MainFragment : Fragment() {
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
        setHasOptionsMenu(true)
        initViewModel()

//        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
//        viewModel.getSongData().observe(this, Observer { songAdapter.updateData(it) })

        Log.d("M_onCreate", "${songList.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        initViews(view)
        Log.d("M_onCreateView", "${songList.size}")
//        initViewModel()
//        setController()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        val searchItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchItem.actionView as SearchView

        val searchEditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextColor(Utils.getColorFromAttr(R.attr.colorEditTextColor, context!!.theme))
        searchEditText.setHintTextColor(Utils.getColorFromAttr(R.attr.colorTextColorHint, context!!.theme))

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
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initViews( view: View) {

        songAdapter = SongAdapter {
            musicService!!.setSongPos(Integer.parseInt(it.tag.toString()))
            musicService!!.playSong()
            if(playbackPaused){
//                setController()
                playbackPaused=false
            }
//            controller!!.show(0)
        }
        Log.d("M_initViews", "${songList.size}")
//        songAdapter.updateData(songList)
        val dividerItemDecorator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MyDividerItemDecorator(resources.getDrawable(R.drawable.divider, context!!.theme))
        } else {
            MyDividerItemDecorator(resources.getDrawable(R.drawable.divider))
        }
        with(view.song_list) {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            addItemDecoration(dividerItemDecorator)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
//        viewModel.getSongData().observe(this, Observer { songList = ArrayList(it) })
        viewModel.getSongData().observe(this, Observer {
            songAdapter.updateData(it)
            songList = ArrayList(it)})
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        initViewModel()

        Log.d("M_onAct1", "${songList.size}")
    }

//    private fun setController() {
//        controller = MusicController(this.context)
//
//        controller!!.setPrevNextListeners(
//            { playNext() },
//            { playPrev() })
//
//        controller!!.setMediaPlayer(playerControl())
//        controller!!.setAnchorView(song_list)
//        controller!!.isEnabled = true
//    }

    private val musicConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            musicBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.service
            Log.d("M_Ser", "${songList.size}")
            musicService!!.setList(songList)
            musicBound = true
        }

    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this.context, MusicService::class.java)
            this.context!!.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            this.context!!.startService(playIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_end -> {
                Log.d("M_onOptionsItemSeletedF", "1")
                this.context!!.stopService(playIntent)
                musicService = null
                exitProcess(0)
            }
        }
        return false
    }

    override fun onDestroy() {
        this.context!!.stopService(playIntent)
        musicService = null
        super.onDestroy()
    }

    private fun playNext() {
        musicService!!.playNext()
        if(playbackPaused) {
//            setController()
            playbackPaused=false
        }
//        controller!!.show(0)
    }

    private fun playPrev() {
        musicService!!.playPrev()
        if(playbackPaused){
//            setController()
            playbackPaused = false
        }
//        controller!!.show(0)
    }

//    inner class playerControl: MediaController.MediaPlayerControl {
//        override fun isPlaying(): Boolean =
//            if (musicService != null && musicBound) {
//                musicService!!.isPlaying()
//            } else false
//
//        override fun canSeekForward(): Boolean = true
//
//        override fun getDuration(): Int =
//            if (musicService != null && musicBound && musicService!!.isPlaying()) {
//                musicService!!.getDuration()
//            } else 0
//
//        override fun pause() {
//            playbackPaused = true
//            musicService!!.pausePlayer()
//        }
//
//        override fun getBufferPercentage(): Int = 0
//
//        override fun seekTo(pos: Int) {
//            musicService!!.seekTo(pos)
//        }
//
//        override fun getCurrentPosition(): Int =
//            if (musicService != null && musicBound && musicService!!.isPlaying()) {
//                musicService!!.getCurrentPosition()
//            } else 0
//
//        override fun canSeekBackward(): Boolean = true
//
//        override fun start() {
//            musicService!!.start()
//        }
//
//        override fun getAudioSessionId(): Int = 0
//
//        override fun canPause(): Boolean = true
//    }

    override fun onPause() {

        Log.d("M_OnPause", "Start")
        super.onPause()
        paused = true
        Log.d("M_OnPause", "End")
    }

    override fun onResume() {
        super.onResume()
        if(paused) {
//            setController()
            paused = false
        }
    }

    override fun onStop() {
//        controller!!.hide()
        super.onStop()
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
