package com.sloy.sevibus.feature.map.icons

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.icons.StopFilled
import com.sloy.sevibus.ui.theme.SevTheme

private val shadowRadius = 2.dp
private val shadowY = 1.dp
// strokeSize = 3.dp

@Composable
@GoogleMapComposable
fun OutlinedStopIcon(stopColor: Color, iconSize: Dp = 40.dp, shadow: Boolean = false) {
    val strokeSize: Dp = iconSize / 12
    val iconSizeWithStroke = iconSize + strokeSize * 2
    Box() {
        if (shadow) {
            // Shadow layer
            Icon(
                SevIcons.StopFilled,
                contentDescription = null,
                tint = SevTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(iconSizeWithStroke)
                    .align(Alignment.CenterStart)
                    .graphicsLayer { translationY = shadowY.toPx() }
                    .blur(shadowRadius, BlurredEdgeTreatment.Unbounded)
            )

        }
        // Outline layer
        Icon(
            SevIcons.StopFilled,
            contentDescription = null,
            tint = SevTheme.colorScheme.background,
            modifier = Modifier
                .size(iconSizeWithStroke)
                .align(Alignment.CenterStart)
        )
        // Colored icon layer
        Icon(
            SevIcons.Stop,
            contentDescription = null,
            tint = stopColor,
            modifier = Modifier
                .size(iconSizeWithStroke)
                .padding(strokeSize)
                .align(Alignment.CenterStart)
        )
    }

}


@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun OutlinedStopIconPreview() {
    Color.Blue
    SevTheme {
        OutlinedStopIcon(LineColor.Red.primary(), shadow = false)

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun OutlinedStopIconShadowPreview() {
    SevTheme {
        OutlinedStopIcon(LineColor.Red.primary(), shadow = true)

    }
}
