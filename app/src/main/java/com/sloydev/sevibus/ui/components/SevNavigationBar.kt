package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun SevNavigationBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    Column {
        HorizontalDivider()
        NavigationBar(modifier,
            containerColor = SevTheme.colorScheme.background) {
            destinations.forEach { destination ->
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
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

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.route, true) ?: false
    } ?: false

@Preview
@Composable
private fun SevNavigationBarPreview() {
    SevNavigationBar(
        TopLevelDestination.entries,
        onNavigateToDestination = { _ -> },
        currentDestination = null
    )
}
