package com.sloy.sevibus.ui.preview

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
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.SevNavigationBar
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun ScreenPreview(content: @Composable () -> Unit) {
    SevTheme {
        Scaffold(bottomBar = {
            SevNavigationBar(
                topLevelDestinations = listOf(
                    NavigationDestination.ForYou,
                    NavigationDestination.Lines,
                    NavigationDestination.Cards(),
                ),
                onNavigateToDestination = { },
                currentNavDestination = null,
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