package com.sloy.sevibus.feature.map.icons

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.icons.StopFilled
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun ShapedStopIcon(stopColor: Color, iconSize: Dp) {
    Box() {
        // Background layer for the inside circle
        Icon(
            SevIcons.StopFilled,
            contentDescription = null,
            tint = SevTheme.colorScheme.background,
            modifier = Modifier
                .size(iconSize / 2)
                .align(Alignment.Center)
        )
        // Colored icon layer
        Icon(
            SevIcons.Stop,
            contentDescription = null,
            tint = stopColor,
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.Center)
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
internal fun ShapedStopIconPreview() {
    SevTheme {
        ShapedStopIcon(LineColor.Red.primary(), 5 * 24.dp)
    }
}
