package cz.skorpil.orlicko.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cz.skorpil.orlicko.player.RadioPlayerState
import cz.skorpil.orlicko.ui.theme.OrlickoDarkRed
import cz.skorpil.orlicko.ui.theme.OrlickoRed

@Composable
fun PlayPauseButton(
    playerState: RadioPlayerState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    val gradient = Brush.linearGradient(
        colors = listOf(OrlickoRed, OrlickoDarkRed),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .drawBehind { drawRect(gradient) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = playerState != RadioPlayerState.BUFFERING,
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (playerState) {
            RadioPlayerState.BUFFERING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White,
                    strokeWidth = 3.dp,
                )
            }
            RadioPlayerState.PLAYING -> {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pause",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
