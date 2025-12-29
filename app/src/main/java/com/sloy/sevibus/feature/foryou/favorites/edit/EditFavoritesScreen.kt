@file:OptIn(ExperimentalFoundationApi::class)

package com.sloy.sevibus.feature.foryou.favorites.edit

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.descriptionWithSeparator
import com.sloy.sevibus.domain.model.toImageVector
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.extensions.performHapticClockTick
import com.sloy.sevibus.infrastructure.extensions.performHapticGestureStart
import com.sloy.sevibus.infrastructure.extensions.performHapticSegmentTick
import com.sloy.sevibus.infrastructure.extensions.plus
import com.sloy.sevibus.infrastructure.extensions.swap
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.components.IconPicker
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.components.SevTopAppBar
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.snackbar.LocalSnackbarHostState
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EditFavoritesScreen() {
    val snackBar = LocalSnackbarHostState.current
    val viewModel = koinViewModel<EditFavoritesViewModel>()
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    EventCollector(viewModel.events) {
        when (it) {
            EditFavoritesEvent.Done -> {
                onBackPressedDispatcher?.onBackPressed()
            }
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    EditFavoritesScreen(
        state,
        snackBar,
        onSaveClick = viewModel::onFavoritesChanged,
        onCancelClick = { onBackPressedDispatcher?.onBackPressed() },
        onTrack = viewModel::track
    )
}

@Composable
fun EditFavoritesScreen(
    state: EditFavoritesState,
    snackBar: SnackbarHostState,
    onSaveClick: (updatedFavorites: List<FavoriteStop>) -> Unit,
    onCancelClick: () -> Unit,
    onTrack: (SevEvent) -> Unit = {}
) {
    val view = LocalView.current
    var favoritesLocalList by remember(state) { mutableStateOf(state.favorites) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        favoritesLocalList = favoritesLocalList.swap(from.index, to.index)
        view.performHapticSegmentTick()
    }

    val scope = rememberCoroutineScope()

    Column(Modifier.padding(horizontal = 16.dp)) {
        SevTopAppBar(
            title = { },
            navigationIcon = {
                CircularIconButton(
                    onClick = { onCancelClick() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel"
                    )
                }
            },
            actions = {
                SurfaceButton("Guardar", onClick = { onSaveClick(favoritesLocalList) })
            }
        )

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.navigation_edit_favorites), style = SevTheme.typography.headingLarge)
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 32.dp) + WindowInsets.navigationBars.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxHeight()
                .imePadding()
        ) {
            items(favoritesLocalList, { it.stop.code }) { favorite ->
                EditFavoriteListItem(
                    favorite,
                    reorderableLazyListState,
                    onNameChanged = { name ->
                        favoritesLocalList = favoritesLocalList.toMutableList().apply {
                            set(indexOfFirst { it.stop.code == favorite.stop.code }, favorite.copy(customName = name))
                        }
                    },
                    onIconChanged = { icon ->
                        favoritesLocalList = favoritesLocalList.toMutableList().apply {
                            set(indexOfFirst { it.stop.code == favorite.stop.code }, favorite.copy(customIcon = icon))
                        }
                    },
                    onLineSelectionChanged = { selectedLines ->
                        favoritesLocalList = favoritesLocalList.toMutableList().apply {
                            set(
                                indexOfFirst { it.stop.code == favorite.stop.code },
                                favorite.copy(selectedLineIds = selectedLines)
                            )
                        }
                    },
                    onTrack = onTrack,
                    onDeleteClicked = {
                        val index = favoritesLocalList.indexOf(favorite)
                        favoritesLocalList = favoritesLocalList.toMutableList().apply {
                            remove(favorite)
                        }
                        scope.launch {
                            snackBar.showSnackbar("Favorita eliminada", "Deshacer", duration = SnackbarDuration.Short).let {
                                if (it == SnackbarResult.ActionPerformed) {
                                    favoritesLocalList = favoritesLocalList.toMutableList().apply {
                                        add(index, favorite)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LazyItemScope.EditFavoriteListItem(
    favorite: FavoriteStop,
    reorderableLazyListState: ReorderableLazyListState,
    onNameChanged: (String) -> Unit,
    onIconChanged: (CustomIcon?) -> Unit,
    onLineSelectionChanged: (Set<LineId>) -> Unit,
    onTrack: (SevEvent) -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var selectedLines by remember(favorite.selectedLineIds, favorite.stop.lines) {
        mutableStateOf(favorite.selectedLineIds ?: favorite.stop.lines.map { it.id }.toSet())
    }

    // Without this, a Surface with onClick parameter will force a minimum size of 48dp
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
    ) {
        ReorderableItem(reorderableLazyListState, key = favorite.stop.code) { isDragging ->
            // Outside card
            Surface(
                color = SevTheme.colorScheme.surface,
                shape = SevTheme.shapes.large,
                modifier = modifier
                    .fillMaxWidth()
                    .alpha(if (isDragging) 0.7f else 1f)
                    .bringIntoViewRequester(bringIntoViewRequester)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                        .padding(start = 16.dp, end = 8.dp)
                        .height(IntrinsicSize.Min)
                ) {
                    // Icon surface with picker
                    var showIconPicker by remember { mutableStateOf(false) }
                    Surface(
                        onClick = { showIconPicker = true },
                        border = BorderStroke(1.dp, SevTheme.colorScheme.outlineVariant),
                        color = SevTheme.colorScheme.outlineVariant,
                        shape = SevTheme.shapes.small,
                    ) {
                        Icon(
                            favorite.customIcon.toImageVector(),
                            contentDescription = null,
                            tint = SevTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                        )
                        IconPicker(
                            expanded = showIconPicker,
                            onDismiss = { showIconPicker = false },
                            onIconSelected = { icon ->
                                showIconPicker = false
                                onIconChanged(icon)
                            }
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        // Text field surface
                        Surface(
                            border = BorderStroke(1.dp, SevTheme.colorScheme.outlineVariant),
                            color = SevTheme.colorScheme.outlineVariant,
                            shape = SevTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {

                            BasicTextField(
                                value = favorite.customName ?: "",
                                onValueChange = onNameChanged,
                                singleLine = true,
                                textStyle = SevTheme.typography.bodyStandardBold.copy(color = SevTheme.colorScheme.onSurface),
                                modifier = Modifier
                                    .onFocusEvent { focusState ->
                                        if (focusState.isFocused) {
                                            scope.launch {
                                                delay(100)
                                                bringIntoViewRequester.bringIntoView()
                                            }
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                }),
                                cursorBrush = SolidColor(SevTheme.colorScheme.onSurface),
                                decorationBox = { innerTextField ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                            innerTextField()
                                            if (favorite.customName?.isEmpty() ?: true) {
                                                Text(
                                                    "Nombre personalizado",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = SevTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (favorite.customName?.isNotEmpty() == true) {
                                            IconButton(onClick = { onNameChanged("") }) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Borrar",
                                                    tint = SevTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        // Stop info
                        Spacer(Modifier.height(8.dp))
                        Text(
                            favorite.stop.descriptionWithSeparator(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = SevTheme.typography.bodySmall,
                            color = SevTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            favorite.stop.lines.forEach { line ->
                                SelectableLineIndicator(
                                    line = line,
                                    isSelected = selectedLines.contains(line.id),
                                    onSelectionChanged = { isSelected ->
                                        view.performHapticClockTick()
                                        val newSelectedLines = if (isSelected) {
                                            selectedLines + line.id
                                        } else {
                                            selectedLines - line.id
                                        }
                                        selectedLines = newSelectedLines
                                        onLineSelectionChanged(newSelectedLines)
                                        onTrack(Clicks.EditFavoriteLineClicked(isSelected))
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Column(modifier = Modifier.fillMaxHeight()) {
                        // Drag handle
                        Icon(
                            Icons.Filled.DragIndicator,
                            contentDescription = stringResource(R.string.cd_drag_reorder_favorites),
                            tint = SevTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(8.dp)
                                .draggableHandle(
                                    onDragStarted = {
                                        view.performHapticGestureStart()
                                    },
                                )
                        )
                        Spacer(Modifier.weight(1f))
                        // Delete button
                        IconButton(
                            onClick = { onDeleteClicked() },
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Borrar",
                                tint = SevTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                    }
                }
            }
        }

    }
}


@Composable
private fun SelectableLineIndicator(
    line: LineSummary,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "lineAlpha"
    )

    Surface(
        onClick = { onSelectionChanged(!isSelected) },
        color = SevTheme.colorScheme.surface.copy(alpha = 0f),
        shape = SevTheme.shapes.small
    ) {
        Box {
            LineIndicator(
                line,
                Modifier
                    .defaultMinSize(32.dp, 32.dp)
                    .alpha(animatedAlpha)
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(1.dp, 1.dp),
                enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)),
                exit = scaleOut(spring(Spring.DampingRatioMediumBouncy))
            ) {
                Surface(
                    color = SevTheme.colorScheme.surface,
                    shape = CircleShape,
                    modifier = Modifier.size(14.dp)
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SevTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(2.dp)
                            .size(12.dp)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    ScreenPreview {
        EditFavoritesScreen(EditFavoritesState(Stubs.favorites), SnackbarHostState(), {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 350)
@Composable
private fun FavoriteItemPreview() {
    SevTheme {
        val reorderableLazyListState = rememberReorderableLazyListState(rememberLazyListState()) { _, _ -> }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.background(SevTheme.colorScheme.background)
        ) {
            item {
                EditFavoriteListItem(Stubs.favorites.first(), reorderableLazyListState, {}, {}, {}, {}, {})
            }
            item {
                EditFavoriteListItem(Stubs.favorites.last(), reorderableLazyListState, {}, {}, {}, {}, {})
            }
        }
    }
}
