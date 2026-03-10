package cz.skorpil.orlicko.player

enum class StreamQuality(
    val label: String,
    val url: String,
    val description: String,
) {
    HIGH("192k", "https://mediaservice.radioorlicko.cz/stream192.mp3", "192 kbps MP3"),
    MEDIUM("128k", "https://mediaservice.radioorlicko.cz/stream128.mp3", "128 kbps MP3"),
    LOW("64k", "https://mediaservice.radioorlicko.cz/stream64.aac", "64 kbps AAC+"),
    LOWEST("32k", "https://mediaservice.radioorlicko.cz/stream32.aac", "32 kbps AAC+"),
}
