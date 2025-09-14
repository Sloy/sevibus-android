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
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.TopLevelDestination
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SevNavigationBar(
    topLevelDestinations: List<TopLevelDestination>,
    onNavigateToDestination: (NavigationDestination) -> Unit,
    currentNavDestination: NavigationDestination?,
    modifier: Modifier = Modifier,
) {
    Column {
        HorizontalDivider(thickness = 2.dp,
            color = SevTheme.colorScheme.surface)
        NavigationBar(
            modifier,
            containerColor = SevTheme.colorScheme.background
        ) {
            topLevelDestinations.forEach { topLevelDestination ->
                val selected = currentNavDestination == topLevelDestination
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
                        selectedIndicatorColor = SevTheme.colorScheme.surface,
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun SevNavigationBarPreview() {
    SevTheme {
        SevNavigationBar(
            listOf(
                NavigationDestination.ForYou,
                NavigationDestination.Lines,
                NavigationDestination.Cards(),
            ),
            onNavigateToDestination = { _ -> },
            currentNavDestination = NavigationDestination.ForYou
        )

    }
}
