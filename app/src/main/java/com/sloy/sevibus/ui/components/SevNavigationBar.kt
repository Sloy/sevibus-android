package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.TopLevelDestination
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SevNavigationBar(
    topLevelDestinations: List<TopLevelDestination>,
    onNavigateToDestination: (NavigationDestination) -> Unit,
    currentNavDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    Column {
        HorizontalDivider()
        NavigationBar(
            modifier,
            containerColor = SevTheme.colorScheme.background
        ) {
            topLevelDestinations.forEach { topLevelDestination ->
                val selected = currentNavDestination?.hierarchy?.any {
                    it.hasRoute(topLevelDestination::class)
                } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(topLevelDestination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) topLevelDestination.selectedIcon else topLevelDestination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(topLevelDestination.iconTextId)) },
                    modifier = modifier,
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIconColor = SevTheme.colorScheme.primary,
                        selectedTextColor = SevTheme.colorScheme.primary,
                        selectedIndicatorColor = SevTheme.colorScheme.primaryContainer,
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun SevNavigationBarPreview() {
    SevNavigationBar(
        listOf(
            NavigationDestination.ForYou,
            NavigationDestination.Lines,
            NavigationDestination.Map,
            NavigationDestination.Cards,
        ),
        onNavigateToDestination = { _ -> },
        currentNavDestination = null
    )
}
