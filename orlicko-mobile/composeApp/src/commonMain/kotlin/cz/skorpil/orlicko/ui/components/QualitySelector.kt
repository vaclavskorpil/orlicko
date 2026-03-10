package cz.skorpil.orlicko.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.skorpil.orlicko.player.StreamQuality
import cz.skorpil.orlicko.ui.theme.CardBorder
import cz.skorpil.orlicko.ui.theme.OrlickoRed
import cz.skorpil.orlicko.ui.theme.TextLabel
import cz.skorpil.orlicko.ui.theme.TextPrimary
import cz.skorpil.orlicko.ui.theme.TextSecondary

private val qualityLabels = mapOf(
    StreamQuality.HIGH to "Vysoká",
    StreamQuality.MEDIUM to "Střední",
    StreamQuality.LOW to "Nízká",
    StreamQuality.LOWEST to "Úsporná",
)

private val qualityFormats = mapOf(
    StreamQuality.HIGH to "MP3",
    StreamQuality.MEDIUM to "MP3",
    StreamQuality.LOW to "AAC+",
    StreamQuality.LOWEST to "AAC+",
)

@Composable
fun QualitySelector(
    selectedQuality: StreamQuality,
    onQualitySelected: (StreamQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    val qualities = StreamQuality.entries
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Kvalita streamu",
            fontSize = 14.sp,
            color = TextLabel,
        )

        // First row: HIGH, MEDIUM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            qualities.take(2).forEach { quality ->
                QualityButton(
                    quality = quality,
                    isSelected = quality == selectedQuality,
                    shape = shape,
                    onClick = { onQualitySelected(quality) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Second row: LOW, LOWEST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            qualities.drop(2).forEach { quality ->
                QualityButton(
                    quality = quality,
                    isSelected = quality == selectedQuality,
                    shape = shape,
                    onClick = { onQualitySelected(quality) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QualityButton(
    quality: StreamQuality,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBg = OrlickoRed.copy(alpha = 0.15f)
    val inactiveBg = androidx.compose.ui.graphics.Color.Transparent

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) activeBg else inactiveBg,
        animationSpec = tween(300),
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) OrlickoRed else CardBorder,
        animationSpec = tween(300),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind { drawRect(backgroundColor) }
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${qualityLabels[quality]} (${quality.label})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = qualityFormats[quality] ?: "",
                fontSize = 12.sp,
                color = TextLabel,
                textAlign = TextAlign.Center,
            )
        }
    }
}
