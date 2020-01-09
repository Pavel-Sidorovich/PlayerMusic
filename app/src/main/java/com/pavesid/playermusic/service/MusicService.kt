package com.pavesid.playermusic.service

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.pavesid.playermusic.models.Song


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    companion object {
        private val NOTIFY_ID = 1
    }

    private var songPos = 0
    private lateinit var songs: ArrayList<Song>
    private val player = MediaPlayer()
    private val musicBind = MusicBinder()
    private var songTitle = ""

    override fun onBind(intent: Intent): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()

//        val notIntent = Intent(this, MainActivity::class.java)
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        val pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
//        } else {
//            Notification.Builder(this)
//        }
//
//        builder.setContentIntent(pendInt)
//            .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
//            .setTicker(songTitle)
//            .setOngoing(true)
//            .setContentTitle("Playing")
//        .setContentText(songTitle)
//        val not = builder.build()
//
//        startForeground(NOTIFY_ID, not)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if(player.currentPosition > 0) {
//            mp.reset()
            playNext()
        }
    }

    override fun onCreate() {
        super.onCreate()
        initMusicPlayer()
    }

    private fun initMusicPlayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player.setAudioAttributes(AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
        } else {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    fun setList(songs: ArrayList<Song>) {
        this.songs = songs
    }

    fun playSong(playSong: Song) {
//        player.reset()
//        val playSong = songs[songPos]

        songTitle = playSong.title
        val currSong = playSong.id
        val trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("My_Music_Service", "Error setting data source", e)
        }
        player.prepareAsync()
    }

    fun playSong() {
//        player.reset()
        val playSong = songs[songPos]

        songTitle = playSong.title
        val currSong = playSong.id
        val trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("My_Music_Service", "Error setting data source", e)
        }
        player.prepareAsync()
    }

    fun setSong(songPos: Int) {
        this.songPos = songPos
    }

    fun getPos(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(pos: Int) {
        player.seekTo(pos)
    }

    fun go() {
        player.start()
    }

    fun playPrev() {
        songPos--
        if(songPos < 0) {
            songPos = songs.size - 1
        }
        playSong()
    }

    fun playNext() {
        songPos++
        if(songPos > songs.size) {
            songPos = 0
        }
        playSong()
    }

//    override fun onDestroy() {
////        stopForeground(true)
//    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }
}
