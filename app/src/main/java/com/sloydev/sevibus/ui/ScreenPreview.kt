package com.sloydev.sevibus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun ScreenPreview(content: @Composable () -> Unit) {
    SevTheme {
        Scaffold(bottomBar = {
            SevNavigationBar(
                destinations = TopLevelDestination.entries,
                onNavigateToDestination = { },
                currentDestination = null,
            )
        }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    )
            ) { content() }
        }
    }
}