package cz.skorpil.orlicko

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform