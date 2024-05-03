package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sloydev.sevibus.ui.theme.SevTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SevTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title, modifier, navigationIcon, actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SevTheme.colorScheme.background)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SevCenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title, modifier, navigationIcon, actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SevTheme.colorScheme.background)
    )
}
