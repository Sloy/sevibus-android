package com.sloy.sevibus.feature.foryou

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sloy.sevibus.R
import com.sloy.sevibus.feature.debug.DebugMenuState
import com.sloy.sevibus.feature.debug.DebugMenuVariant
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidget
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.SevCenterAlignedTopAppBar
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme


fun NavGraphBuilder.forYouRoute(navController: NavHostController) {
    composable<NavigationDestination.ForYou> {
        ForYouScreen(onStopClicked = { code ->
            navController.navigate(NavigationDestination.StopDetail(code))
        })
    }
}

@Composable
fun ForYouScreen(onStopClicked: (code: Int) -> Unit) {
    Column {
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
