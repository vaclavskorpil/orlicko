package cz.skorpil.orlicko.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrlickoRed = Color(0xFFFF0000)
val OrlickoDarkRed = Color(0xFFCC0000)

val BackgroundDark = Color(0xFF1A1A1A)
val BackgroundLighter = Color(0xFF2D2D2D)

val CardBackground = Color(0x80000000)
val CardBorder = Color(0x1AFFFFFF)

val TextPrimary = Color.White
val TextSecondary = Color(0xFFCCCCCC)
val TextLabel = Color(0xFF999999)

private val OrlickoColorScheme = darkColorScheme(
    primary = OrlickoRed,
    onPrimary = Color.White,
    secondary = OrlickoDarkRed,
    background = BackgroundDark,
    surface = BackgroundLighter,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = OrlickoRed,
)

@Composable
fun OrlickoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OrlickoColorScheme,
        content = content,
    )
}
