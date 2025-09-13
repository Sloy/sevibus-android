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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidget
import com.sloy.sevibus.feature.foryou.nearby.NearbyWidget
import com.sloy.sevibus.infrastructure.extensions.performHapticSegmentTick
import com.sloy.sevibus.ui.components.SegmentedControl
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun ForYouScreen(onStopClicked: (code: Int) -> Unit, onEditFavoritesClicked: () -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<ForYouViewModel>()
        val selectedIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
        ForYouScreen(selectedIndex, onStopClicked, onEditFavoritesClicked, onTabSelected = viewModel::onTabSelected)
    } else {
        ForYouScreen(0, onStopClicked, onEditFavoritesClicked, onTabSelected = {})
    }
}

@Composable
private fun ForYouScreen(
    selectedIndex: Int,
    onStopClicked: (code: Int) -> Unit,
    onEditFavoritesClicked: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.foryou_title), style = SevTheme.typography.headingLarge, modifier = Modifier.padding(start = 16.dp))

        val view = LocalView.current
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            options = listOf(stringResource(R.string.foryou_tab_favorites), stringResource(R.string.foryou_tab_nearby)),
            selectedIndex = selectedIndex,
            onOptionSelected = {
                onTabSelected(it)
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
        ForYouScreen(0, {}, {}, {})
    }
}
