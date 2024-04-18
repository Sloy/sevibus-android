package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sloydev.sevibus.R
import com.sloydev.sevibus.ui.ScreenPreview

@Composable
fun CardsRoute() {
    CardsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen() {
    Column {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.navigation_cards)) })
    }
}


@Preview
@Composable
private fun CardsScreenPreview() {
    ScreenPreview {
        CardsScreen()
    }
}