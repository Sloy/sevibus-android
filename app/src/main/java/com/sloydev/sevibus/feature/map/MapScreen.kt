package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.feature.lines.SearchResult
import com.sloydev.sevibus.feature.lines.SevSearchBar
import com.sloydev.sevibus.feature.stopdetail.StopDetailScreen
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.icons.DirectionsBusFill
import com.sloydev.sevibus.ui.icons.SevIcons

fun NavGraphBuilder.mapRoute() {
    composable(TopLevelDestination.MAP.route) {
        MapScreen(Stubs.searchResults.take(3))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapScreen(previewFilters: List<SearchResult> = emptyList()) {
    Box(Modifier.fillMaxSize()) {
        var showBottomSheet by remember { mutableStateOf(false) }
        val filters = remember { mutableStateListOf<SearchResult>(*previewFilters.toTypedArray()) }
        Map(
            Modifier
                .fillMaxSize()
                .zIndex(0f), onStopClick = { showBottomSheet = true })
        Column {
            SevSearchBar(
                onSearchResultClicked = { filters.add(it) },
                Modifier.padding(horizontal = 16.dp),
            )
            FlowRow(
                Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(align = Alignment.Top),
                horizontalArrangement = Arrangement.Start,
            ) {
                //NearbyChip(Modifier.padding(end = 8.dp))
                filters.forEach { filter ->
                    MapFilterChip(filter, onRemoveFilter = { assert(filters.remove(it)) }, modifier = Modifier.padding(end = 8.dp))
                }

            }
        }

        if (showBottomSheet) {
            BottomSheetScaffold(
                sheetPeekHeight = (96).dp,
                sheetContent = { StopDetailScreen(Stubs.stops[0], embedded = true) },
                content = {}
            )
        }
    }
}

@Composable
fun MapFilterChip(filter: SearchResult, onRemoveFilter: (SearchResult) -> Unit, modifier: Modifier = Modifier) {
    //var selected by remember { mutableStateOf(true) }
    val label: @Composable () -> Unit = when (filter) {
        is SearchResult.LineResult -> {
            { LineIndicatorSmall(filter.line) }
        }

        is SearchResult.StopResult -> {
            { Text(filter.stop.code.toString()) }
        }
    }
    ElevatedAssistChip(
        modifier = modifier,
//        selected = selected,
        onClick = { /*selected = !selected*/ },
        label = { label() },
        leadingIcon = {
            /*if (selected) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }*/
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                onClick = { onRemoveFilter(filter) }) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    )
}

@Composable
fun NearbyChip(modifier: Modifier) {
    var nearby by remember { mutableStateOf(true) }
    ElevatedFilterChip(
        modifier = modifier,
        selected = nearby,
        onClick = { nearby = !nearby },
        label = { Text("Cercanas") },
        leadingIcon = {
            if (nearby) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Localized Description",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        }
    )
}

@Composable
fun Map(modifier: Modifier, onStopClick: (code: Int) -> Unit) {
    //TODO real map
    Box(modifier = modifier.background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
        IconButton(onClick = { onStopClick(154) }) {
            Icon(SevIcons.DirectionsBusFill, contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen(previewFilters = Stubs.searchResults.take(3))
    }
}