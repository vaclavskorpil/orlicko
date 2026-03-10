package cz.skorpil.orlicko.player

import kotlinx.coroutines.flow.StateFlow

interface RadioPlayer {
    val state: StateFlow<RadioPlayerState>
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun setVolume(volume: Float)
    fun updateMetadata(title: String, artist: String = "Radio Orlicko")
    fun release()
}
