package com.sloydev.sevibus.feature.foryou

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sloydev.sevibus.R
import com.sloydev.sevibus.feature.lines.FavoritesCard
import com.sloydev.sevibus.ui.ScreenPreview

@Composable
fun ForYouRoute() {
    ForYouScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouScreen() {
    Column {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
        FavoritesCard()
    }
}


@Preview
@Composable
private fun ForYouScreenPreview() {
    ScreenPreview {
        ForYouScreen()
    }
}