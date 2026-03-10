package cz.skorpil.orlicko.player

import android.content.ComponentName
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cz.skorpil.orlicko.AndroidContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream

class AndroidRadioPlayer : RadioPlayer {

    private val _state = MutableStateFlow(RadioPlayerState.IDLE)
    override val state: StateFlow<RadioPlayerState> = _state.asStateFlow()

    private var mediaController: MediaController? = null
    private var pendingUrl: String? = null

    private val artworkData: ByteArray? by lazy {
        try {
            val context = AndroidContext.appContext
            val inputStream = context.assets.open("composeResources/orlicko.composeapp.generated.resources/drawable/logo.webp")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    init {
        val context = AndroidContext.appContext
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                val controller = controllerFuture.get()
                mediaController = controller
                controller.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updateState(controller)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updateState(controller)
                    }
                })
                // Sync initial state from service (e.g. if service was already playing)
                updateState(controller)
                // Execute pending command if controller wasn't ready when play() was called
                pendingUrl?.let { url ->
                    pendingUrl = null
                    playInternal(controller, url)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun updateState(controller: MediaController) {
        _state.value = when {
            controller.playbackState == Player.STATE_BUFFERING -> RadioPlayerState.BUFFERING
            controller.isPlaying -> RadioPlayerState.PLAYING
            controller.playbackState == Player.STATE_READY && !controller.playWhenReady -> RadioPlayerState.PAUSED
            controller.playbackState == Player.STATE_IDLE -> RadioPlayerState.IDLE
            else -> _state.value
        }
    }

    override fun play(url: String) {
        val controller = mediaController
        if (controller == null) {
            pendingUrl = url
            _state.value = RadioPlayerState.BUFFERING
            return
        }
        playInternal(controller, url)
    }

    private fun playInternal(controller: MediaController, url: String) {
        val metadataBuilder = MediaMetadata.Builder()
            .setStation("Rádio Orlicko")
            .setTitle("Rádio Orlicko")
        artworkData?.let { data ->
            metadataBuilder.setArtworkData(data, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(metadataBuilder.build())
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.playWhenReady = true
    }

    override fun pause() {
        mediaController?.playWhenReady = false
    }

    override fun resume() {
        mediaController?.playWhenReady = true
    }

    override fun stop() {
        mediaController?.stop()
        _state.value = RadioPlayerState.IDLE
    }

    override fun setVolume(volume: Float) {
        mediaController?.volume = volume
    }

    override fun updateMetadata(title: String, artist: String) {
        val controller = mediaController ?: return
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setStation("Rádio Orlicko")
        artworkData?.let { data ->
            metadataBuilder.setArtworkData(data, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        val currentItem = controller.currentMediaItem ?: return
        val updated = currentItem.buildUpon()
            .setMediaMetadata(metadataBuilder.build())
            .build()
        controller.replaceMediaItem(0, updated)
    }

    override fun release() {
        mediaController?.let { controller ->
            controller.stop()
            controller.release()
        }
        mediaController = null
    }
}
