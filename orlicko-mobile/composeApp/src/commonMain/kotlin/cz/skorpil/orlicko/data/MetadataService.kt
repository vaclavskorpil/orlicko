package cz.skorpil.orlicko.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class MetadataService {
    private val client = HttpClient()

    suspend fun fetchCurrentSong(): String {
        return try {
            client.get("https://radioorlicko.cz/data/current_song.txt")
                .bodyAsText()
                .trim()
        } catch (_: Exception) {
            ""
        }
    }
}
