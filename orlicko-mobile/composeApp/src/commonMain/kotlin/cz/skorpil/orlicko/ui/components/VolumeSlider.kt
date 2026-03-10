package cz.skorpil.orlicko.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.skorpil.orlicko.ui.theme.OrlickoRed
import cz.skorpil.orlicko.ui.theme.TextLabel
import cz.skorpil.orlicko.ui.theme.TextSecondary

@Composable
fun VolumeSlider(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val emoji = when {
        volume == 0f -> "\uD83D\uDD07"   // 🔇
        volume < 0.33f -> "\uD83D\uDD08" // 🔈
        volume < 0.66f -> "\uD83D\uDD09" // 🔉
        else -> "\uD83D\uDD0A"           // 🔊
    }

    val percentage = "${(volume * 100).toInt()}%"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Hlasitost",
            fontSize = 14.sp,
            color = TextLabel,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp,
            )

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = OrlickoRed,
                    activeTrackColor = OrlickoRed,
                ),
            )

            Text(
                text = percentage,
                fontSize = 14.sp,
                color = TextSecondary,
            )
        }
    }
}
