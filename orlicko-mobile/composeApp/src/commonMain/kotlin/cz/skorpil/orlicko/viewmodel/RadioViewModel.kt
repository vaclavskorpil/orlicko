package cz.skorpil.orlicko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.skorpil.orlicko.data.MetadataService
import cz.skorpil.orlicko.player.RadioPlayerState
import cz.skorpil.orlicko.player.StreamQuality
import cz.skorpil.orlicko.player.createRadioPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RadioViewModel : ViewModel() {
    private val player = createRadioPlayer()
    private val metadataService = MetadataService()

    private val _currentSong = MutableStateFlow("")
    val currentSong: StateFlow<String> = _currentSong.asStateFlow()

    val playerState: StateFlow<RadioPlayerState> = player.state

    private val _selectedQuality = MutableStateFlow(StreamQuality.HIGH)
    val selectedQuality: StateFlow<StreamQuality> = _selectedQuality.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                val song = metadataService.fetchCurrentSong()
                if (song.isNotEmpty()) {
                    _currentSong.value = song
                    if (playerState.value == RadioPlayerState.PLAYING ||
                        playerState.value == RadioPlayerState.BUFFERING
                    ) {
                        player.updateMetadata(title = song)
                    }
                }
                delay(10_000)
            }
        }
    }

    fun togglePlayPause() {
        when (playerState.value) {
            RadioPlayerState.IDLE, RadioPlayerState.ERROR -> {
                player.play(_selectedQuality.value.url)
            }
            RadioPlayerState.PLAYING -> player.pause()
            RadioPlayerState.PAUSED -> player.resume()
            RadioPlayerState.BUFFERING -> { /* ignore during buffering */ }
        }
    }

    fun selectQuality(quality: StreamQuality) {
        _selectedQuality.value = quality
        if (playerState.value == RadioPlayerState.PLAYING ||
            playerState.value == RadioPlayerState.BUFFERING
        ) {
            player.stop()
            player.play(quality.url)
        }
    }

    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        player.setVolume(_volume.value)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
