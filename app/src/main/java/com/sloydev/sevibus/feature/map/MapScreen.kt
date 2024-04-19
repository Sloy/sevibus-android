package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.R
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.ScreenPreview

fun NavGraphBuilder.mapRoute() {
    composable(TopLevelDestination.MAP.route) {
        MapScreen()
    }
}

@Composable
fun MapRoute() {
    MapScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    Column {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.navigation_map)) })
    }
}


@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen()
    }
}