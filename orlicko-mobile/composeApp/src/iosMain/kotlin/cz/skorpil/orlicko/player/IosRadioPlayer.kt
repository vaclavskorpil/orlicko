package cz.skorpil.orlicko.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.setVolume
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.volume
import platform.Foundation.NSURL
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class IosRadioPlayer : RadioPlayer {

    private var avPlayer: AVPlayer? = null
    private val _state = MutableStateFlow(RadioPlayerState.IDLE)
    override val state: StateFlow<RadioPlayerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollingJob: Job? = null
    private var currentUrl: String? = null

    init {
        setupRemoteCommands()
    }

    override fun play(url: String) {
        _state.value = RadioPlayerState.BUFFERING
        currentUrl = url

        configureAudioSession()

        val nsUrl = NSURL.URLWithString(url) ?: run {
            _state.value = RadioPlayerState.ERROR
            return
        }
        val playerItem = AVPlayerItem(uRL = nsUrl)
        avPlayer = AVPlayer(playerItem = playerItem)
        avPlayer?.volume = 1.0f
        avPlayer?.play()
        startPolling()
    }

    override fun pause() {
        avPlayer?.pause()
        _state.value = RadioPlayerState.PAUSED
        stopPolling()
        updateNowPlayingPlaybackRate(0.0)
    }

    override fun resume() {
        _state.value = RadioPlayerState.BUFFERING
        configureAudioSession()
        avPlayer?.play()
        startPolling()
        updateNowPlayingPlaybackRate(1.0)
    }

    override fun stop() {
        avPlayer?.pause()
        avPlayer = null
        currentUrl = null
        _state.value = RadioPlayerState.IDLE
        stopPolling()
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
    }

    override fun setVolume(volume: Float) {
        avPlayer?.volume = volume
    }

    override fun updateMetadata(title: String, artist: String) {
        val info = mutableMapOf<Any?, Any?>(
            MPMediaItemPropertyTitle to title,
            MPMediaItemPropertyArtist to artist,
            MPNowPlayingInfoPropertyPlaybackRate to if (_state.value == RadioPlayerState.PLAYING) 1.0 else 0.0
        )
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
    }

    override fun release() {
        stop()
        scope.cancel()
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.removeTarget(null)
        commandCenter.pauseCommand.removeTarget(null)
        commandCenter.togglePlayPauseCommand.removeTarget(null)
    }

    private fun configureAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    private fun setupRemoteCommands() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()

        commandCenter.playCommand.setEnabled(true)
        commandCenter.playCommand.addTargetWithHandler { _ ->
            if (avPlayer != null) {
                resume()
            }
            0L // MPRemoteCommandHandlerStatusSuccess
        }

        commandCenter.pauseCommand.setEnabled(true)
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            pause()
            0L
        }

        commandCenter.togglePlayPauseCommand.setEnabled(true)
        commandCenter.togglePlayPauseCommand.addTargetWithHandler { _ ->
            when (_state.value) {
                RadioPlayerState.PLAYING -> pause()
                RadioPlayerState.PAUSED -> resume()
                else -> { /* ignore */ }
            }
            0L
        }
    }

    private fun updateNowPlayingPlaybackRate(rate: Double) {
        val info = MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo?.toMutableMap() ?: return
        info[MPNowPlayingInfoPropertyPlaybackRate] = rate
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                val p = avPlayer ?: break
                when (p.timeControlStatus) {
                    AVPlayerTimeControlStatusPlaying ->
                        _state.value = RadioPlayerState.PLAYING

                    AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate ->
                        _state.value = RadioPlayerState.BUFFERING

                    AVPlayerTimeControlStatusPaused -> {
                        if (_state.value == RadioPlayerState.BUFFERING) {
                            _state.value = RadioPlayerState.BUFFERING
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
