package com.sloy.sevibus.feature.foryou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidget
import com.sloy.sevibus.feature.foryou.nearby.NearbyWidget
import com.sloy.sevibus.infrastructure.extensions.performHapticSegmentTick
import com.sloy.sevibus.ui.components.SegmentedControl
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme


@Composable
fun ForYouScreen(onStopClicked: (code: Int) -> Unit, onEditFavoritesClicked: () -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text("Para ti", style = SevTheme.typography.headingLarge, modifier = Modifier.padding(start = 16.dp))

        val view = LocalView.current
        var selectedIndex by remember { mutableStateOf(0) }
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            options = listOf("Favoritas", "Cercanas"),
            selectedIndex = selectedIndex,
            onOptionSelected = {
                selectedIndex = it
                view.performHapticSegmentTick()
            }
        )

        SlidingContent(selectedIndex, onStopClicked, onEditFavoritesClicked)
    }
}

@Composable
private fun SlidingContent(
    selectedIndex: Int,
    onStopClicked: (code: Int) -> Unit,
    onEditFavoritesClicked: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = selectedIndex == 0,
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
            ),
            enter = slideInHorizontally(
                initialOffsetX = { -it },
            )
        ) {
            FavoritesWidget(onStopClicked, onEditFavoritesClicked)
        }
        AnimatedVisibility(
            visible = selectedIndex == 1,
            exit = slideOutHorizontally(
                targetOffsetX = { it },
            ),
            enter = slideInHorizontally(
                initialOffsetX = { it },
            )
        ) {
            NearbyWidget(onStopClicked)
        }
    }
}


@Preview
@Composable
private fun ForYouScreenPreview() {
    ScreenPreview {
        ForYouScreen({}, {})
    }
}
