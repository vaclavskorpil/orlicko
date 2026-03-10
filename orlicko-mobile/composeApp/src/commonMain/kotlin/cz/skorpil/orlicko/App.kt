package cz.skorpil.orlicko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.skorpil.orlicko.ui.PlayerScreen
import cz.skorpil.orlicko.ui.theme.BackgroundDark
import cz.skorpil.orlicko.ui.theme.BackgroundLighter
import cz.skorpil.orlicko.ui.theme.OrlickoTheme
import cz.skorpil.orlicko.viewmodel.RadioViewModel

@Composable
fun App() {
    OrlickoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(BackgroundDark, BackgroundLighter),
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                ),
        ) {
            val viewModel: RadioViewModel = viewModel { RadioViewModel() }
            PlayerScreen(viewModel = viewModel)
        }
    }
}
