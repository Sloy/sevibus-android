package com.sloy.sevibus.feature.map.icons

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun CircularStopIcon(stopColor: Color, iconSize: Dp) {
    Icon(Icons.Filled.Circle, contentDescription = null, modifier = Modifier.size(iconSize), tint = stopColor)
}


@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0xFF00FFFF, name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
internal fun CircularStopIconPreview() {
    SevTheme {
        CircularStopIcon(LineColor.Red.primary(), iconSize = 8.dp)
    }
}
