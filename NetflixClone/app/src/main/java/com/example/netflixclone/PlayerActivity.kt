package com.example.netflixclone

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var playerLoading: ProgressBar
    private lateinit var tvPlayerTitle: TextView
    private lateinit var tvPlayerSubtitle: TextView
    private lateinit var tvPlayerDescription: TextView
    private lateinit var tvPlayerStatus: TextView
    private lateinit var btnClosePlayer: TextView

    private var player: ExoPlayer? = null
    private var videoRawName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        playerLoading = findViewById(R.id.playerLoading)
        tvPlayerTitle = findViewById(R.id.tvPlayerTitle)
        tvPlayerSubtitle = findViewById(R.id.tvPlayerSubtitle)
        tvPlayerDescription = findViewById(R.id.tvPlayerDescription)
        tvPlayerStatus = findViewById(R.id.tvPlayerStatus)
        btnClosePlayer = findViewById(R.id.btnClosePlayer)

        val title = intent.getStringExtra("title") ?: "Película"
        val subtitle = intent.getStringExtra("subtitle") ?: "Contenido"
        val description = intent.getStringExtra("description") ?: "Video de muestra"
        videoRawName = intent.getStringExtra("videoRawName").orEmpty()

        tvPlayerTitle.text = title
        tvPlayerSubtitle.text = "$subtitle • Reproducción local"
        tvPlayerDescription.text = description

        btnClosePlayer.setOnClickListener {
            finish()
        }

        initializePlayer()
    }

    private fun initializePlayer() {
        if (videoRawName.isBlank()) {
            playerLoading.visibility = View.GONE
            tvPlayerStatus.text = "Este contenido no tiene demo disponible."
            Toast.makeText(this, "No hay demo disponible", Toast.LENGTH_LONG).show()
            return
        }

        val videoId = resources.getIdentifier(
            videoRawName,
            "raw",
            packageName
        )

        if (videoId == 0) {
            playerLoading.visibility = View.GONE
            tvPlayerStatus.text = "No se encontró el video: $videoRawName.mp4"
            Toast.makeText(
                this,
                "Falta el video $videoRawName.mp4 en res/raw",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val videoUri = Uri.parse("android.resource://$packageName/$videoId")

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player?.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        playerLoading.visibility = View.VISIBLE
                        tvPlayerStatus.text = "Cargando video..."
                    }

                    Player.STATE_READY -> {
                        playerLoading.visibility = View.GONE
                        tvPlayerStatus.text = "Reproduciendo video local"
                    }

                    Player.STATE_ENDED -> {
                        playerLoading.visibility = View.GONE
                        tvPlayerStatus.text = "Video finalizado"
                    }

                    Player.STATE_IDLE -> {
                        tvPlayerStatus.text = "Preparando reproducción..."
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    playerLoading.visibility = View.GONE
                    tvPlayerStatus.text = "Reproduciendo video local"
                }
            }
        })

        player?.apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        playerView.player = null
        player?.release()
        player = null
    }
}