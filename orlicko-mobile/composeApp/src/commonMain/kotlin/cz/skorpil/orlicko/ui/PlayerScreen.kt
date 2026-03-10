package cz.skorpil.orlicko.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.skorpil.orlicko.player.RadioPlayerState
import cz.skorpil.orlicko.ui.components.NowPlayingInfo
import cz.skorpil.orlicko.ui.components.PlayPauseButton
import cz.skorpil.orlicko.ui.components.QualitySelector
import cz.skorpil.orlicko.ui.theme.CardBackground
import cz.skorpil.orlicko.ui.theme.CardBorder
import cz.skorpil.orlicko.ui.theme.OrlickoRed
import cz.skorpil.orlicko.viewmodel.RadioViewModel
import orlicko.composeapp.generated.resources.Res
import orlicko.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerScreen(viewModel: RadioViewModel) {
    val currentSong by viewModel.currentSong.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Player card
        val cardShape = RoundedCornerShape(20.dp)
        val cardBg = CardBackground
        val cardBorderColor = CardBorder

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .clip(cardShape)
                .drawBehind { drawRect(cardBg) }
                .border(1.dp, cardBorderColor, cardShape)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Radio Orlicko Logo",
                modifier = Modifier.height(120.dp),
            )

            Spacer(modifier = Modifier.height(56.dp))

            // ON AIR badge
            OnAirBadge()

            Spacer(modifier = Modifier.height(20.dp))

            NowPlayingInfo(currentSong = currentSong)

            Spacer(modifier = Modifier.height(24.dp))

            PlayPauseButton(
                playerState = playerState,
                onToggle = viewModel::togglePlayPause,
            )

            if (playerState == RadioPlayerState.ERROR) {
                Text(
                    text = "Chyba přehrávání",
                    color = OrlickoRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            QualitySelector(
                selectedQuality = selectedQuality,
                onQualitySelected = viewModel::selectQuality,
            )
        }

        // Footer
        val uriHandler = LocalUriHandler.current
        Text(
            text = "radioorlicko.cz",
            fontSize = 14.sp,
            color = OrlickoRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable { uriHandler.openUri("https://radioorlicko.cz") },
        )
    }
}


@Composable
private fun OnAirBadge() {
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val badgeShape = RoundedCornerShape(20.dp)
    val badgeBg = OrlickoRed

    Row(
        modifier = Modifier
            .clip(badgeShape)
            .drawBehind { drawRect(badgeBg) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Pulsing dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(Color.White.copy(alpha = dotAlpha))
                },
        )

        Text(
            text = "ON AIR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp,
        )
    }
}
