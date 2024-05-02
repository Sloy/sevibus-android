package com.sloydev.sevibus.feature.foryou

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sloydev.sevibus.R
import com.sloydev.sevibus.feature.debug.DebugMenuState
import com.sloydev.sevibus.feature.debug.DebugMenuVariant
import com.sloydev.sevibus.feature.foryou.favorites.FavoritesWidget
import com.sloydev.sevibus.feature.stopdetail.navigateToStopDetail
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.components.SevCenterAlignedTopAppBar
import com.sloydev.sevibus.ui.preview.ScreenPreview


fun NavGraphBuilder.forYouRoute(navController: NavHostController) {
    composable(TopLevelDestination.FOR_YOU.route) {
        ForYouScreen(onStopClicked = { code ->
            navController.navigateToStopDetail(code)
        })
    }
}

@Composable
fun ForYouScreen(onStopClicked: (code: Int) -> Unit) {
    Column {
        SevCenterAlignedTopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                if (DebugMenuVariant.isDebug) {
                    IconButton(onClick = { DebugMenuState.openMenu() }) {
                        Icon(
                            Icons.Default.BugReport, contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            }
        )
        FavoritesWidget(onStopClicked)
    }
}


@Preview
@Composable
private fun ForYouScreenPreview() {
    ScreenPreview {
        ForYouScreen({})
    }
}