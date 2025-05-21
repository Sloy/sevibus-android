package com.sloy.sevibus.feature.map.icons

import Chevron
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun BusMapIcon(line: LineSummary? = null) {
    Column {
        if (line != null) {
            LineIndicatorChevron(line, Modifier.padding(start = 6.dp, bottom = 2.dp))
        }
        Image(painterResource(R.drawable.icon_bus_shadow), contentDescription = null)
    }
}

@Composable
private fun LineIndicatorChevron(line: LineSummary, modifier: Modifier = Modifier) {
    SevTheme.WithLineColors(line.color) {
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 4.dp)) {
            Box(
                modifier
                    .zIndex(1f)
                    .defaultMinSize(20.dp, 20.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(SevTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    line.label,
                    color = SevTheme.colorScheme.onPrimary,
                    style = SevTheme.typography.bodyExtraSmallBold,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            Icon(
                SevIcons.Chevron,
                contentDescription = null,
                tint = SevTheme.colorScheme.primary,
                modifier = modifier
                    .zIndex(0f)
                    .graphicsLayer {
                        translationY = 4.dp.toPx()
                    })
        }
    }
}

@Preview()
@Composable
private fun Preview() {
    SevTheme {
        BusMapIcon(Stubs.lines.first().toSummary())
    }
}
