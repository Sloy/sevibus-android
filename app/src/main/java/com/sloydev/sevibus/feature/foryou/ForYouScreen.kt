package com.sloydev.sevibus.feature.foryou

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sloydev.sevibus.R
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
        SevCenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
        FavoritesListElement(onStopClicked)
    }
}


@Preview
@Composable
private fun ForYouScreenPreview() {
    ScreenPreview {
        ForYouScreen({})
    }
}