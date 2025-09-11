package com.sloy.sevibus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch

@Composable
fun AppUpdateButton(state: AppUpdateButtonState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse = if (state is AppUpdateButtonState.Ready) {
        infiniteTransition.animateFloat(
            label = "pulse",
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
        )
    } else {
        null
    }

    AnimatedVisibility(
        visible = state !is AppUpdateButtonState.Hidden,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {

        Surface(
            onClick = {
                scope.launch {
                    if (state is AppUpdateButtonState.Available) state.onClick()
                    if (state is AppUpdateButtonState.Ready) state.onClick()
                }
            },
            shape = SevTheme.shapes.extraLarge,
            color = SevTheme.colorScheme.background,
            contentColor = contentColorFor(SevTheme.colorScheme.onBackground),
            shadowElevation = 4.dp,
            modifier = Modifier.graphicsLayer {
                pulse?.value?.let {
                    scaleX = it
                    scaleY = it
                }
            }
        ) {
            val text = when (state) {
                is AppUpdateButtonState.Available -> "Actualización disponible"
                is AppUpdateButtonState.Downloading -> "Descargando… ${percentage(state.downloaded, state.total)}%"
                is AppUpdateButtonState.Ready -> "Pulsa para instalar"
                is AppUpdateButtonState.Hidden -> ""
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(4.dp)
                    .padding(end = 8.dp)
            ) {
                when (state) {
                    is AppUpdateButtonState.Available -> DownloadIcon()
                    is AppUpdateButtonState.Downloading -> ProgressIcon(progress(state.downloaded, state.total))
                    is AppUpdateButtonState.Ready -> ReadyIcon()
                    else -> {}
                }

                Text(text, style = SevTheme.typography.bodySmallBold)

            }
        }
    }
}

private fun progress(completed: Long, total: Long): Float {
    if (total == 0L) return 0f
    if (completed == 0L) return 0f
    if (completed > total) return 1f
    return (completed.toFloat() / total.toFloat())
}

private fun percentage(completed: Long, total: Long): Int {
    return (progress(completed, total) * 100).toInt()
}

@Composable
private fun ProgressIcon(progress: Float) {
    Box(
        Modifier
            .padding(end = 8.dp)
            .padding(4.dp)
            .size(20.dp)
    ) {
        if (progress < 0.1) {
            CircularProgressIndicator(strokeWidth = 3.dp)
        } else {
            CircularProgressIndicator(progress = { progress }, strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun DownloadIcon() {
    Box(
        Modifier
            .padding(end = 8.dp)
            .background(SevTheme.colorScheme.primary, CircleShape)
            .padding(4.dp)
    ) {
        Icon(
            Icons.Default.InstallMobile,
            contentDescription = null,
            tint = SevTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ReadyIcon() {
    Box(
        Modifier
            .padding(end = 8.dp)
            .background(SevTheme.colorScheme.primary, CircleShape)
            .padding(4.dp)
    ) {
        Icon(
            Icons.Default.MobileFriendly,
            contentDescription = null,
            tint = SevTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview
@Composable
private fun AvailablePreview() {
    SevTheme {
        AppUpdateButton(AppUpdateButtonState.Available({}), Modifier.padding(8.dp))
    }
}

@Preview
@Composable
private fun DownloadingZeroPreview() {
    SevTheme {
        AppUpdateButton(AppUpdateButtonState.Downloading(0L, 0L), Modifier.padding(8.dp))
    }
}

@Preview
@Composable
private fun DownloadingPreview() {
    SevTheme {
        AppUpdateButton(AppUpdateButtonState.Downloading(30L, 100L), Modifier.padding(8.dp))
    }
}

@Preview
@Composable
private fun ReadyPreview() {
    SevTheme {
        AppUpdateButton(AppUpdateButtonState.Ready({}), Modifier.padding(8.dp))
    }
}

sealed class AppUpdateButtonState {
    object Hidden : AppUpdateButtonState()
    data class Available(val onClick: suspend () -> Unit) : AppUpdateButtonState()
    data class Downloading(val downloaded: Long, val total: Long) : AppUpdateButtonState()
    data class Ready(val onClick: suspend () -> Unit) : AppUpdateButtonState()

}
