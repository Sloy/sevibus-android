package com.sloy.sevibus.feature.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun RoundedSearchBar(
    state: TopBarState,
    onSearchTermChanged: (String) -> Unit,
    onOpenSearchScreen: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isSearchActive = (state as? TopBarState.Search)?.isSearchActive ?: false
    val searchTerm = when (state) {
        is TopBarState.Search -> state.term
        is TopBarState.LineSelected -> state.line.description
        is TopBarState.StopSelected -> state.stop.description
    }

    val elevation = animateDpAsState(if (isSearchActive) 0.dp else 8.dp).value
    val backgroundColor = animateColorAsState(
        if (isSearchActive) SevTheme.colorScheme.surface else SevTheme.colorScheme.background,
    ).value

    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) {
            focusManager.clearFocus()
        }
    }

    Surface(
        onClick = { focusRequester.requestFocus() },
        shape = SevTheme.shapes.extraLarge,
        color = backgroundColor,
        contentColor = contentColorFor(backgroundColor),
        shadowElevation = elevation,
        modifier = modifier
            .zIndex(1f)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            when (state) {
                is TopBarState.Search -> Icon(
                    Icons.Filled.Search,
                    tint = SevTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Search"
                )

                is TopBarState.StopSelected -> Icon(SevIcons.Stop, tint = SevTheme.colorScheme.primary, contentDescription = "Stop")
                is TopBarState.LineSelected -> LineIndicator(state.line)
            }
            Spacer(Modifier.width(16.dp))
            BasicTextField(
                value = searchTerm,
                onValueChange = { onSearchTermChanged(it) },
                singleLine = true,
                textStyle = SevTheme.typography.bodyStandard.copy(color = SevTheme.colorScheme.onSurface),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onOpenSearchScreen()
                        } else {
                            //TODO hide keyboard
                        }
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {

                    focusManager.clearFocus()
                }),
                cursorBrush = SolidColor(SevTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            innerTextField()
                            if (searchTerm.isEmpty()) {
                                Text(stringResource(R.string.search_placeholder), color = SevTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = {
                                if (state is TopBarState.Search) {
                                    onSearchTermChanged("")
                                } else {
                                    onCancelClicked()
                                }
                            }) {
                                Icon(Icons.Filled.Clear, tint = SevTheme.colorScheme.onSurfaceVariant, contentDescription = "Clear search")
                            }
                        }
                    }
                }
            )
        }
    }
}


@PreviewLightDark()
@Composable
internal fun SearchPreviewLightDark() {
    SevTheme {
        RoundedSearchBar(
            state = TopBarState.Search("triana", true),
            onSearchTermChanged = {},
            onOpenSearchScreen = {},
            onCancelClicked = {},
        )
    }
}

@PreviewLightDark()
@Composable
internal fun SearchEmptyPreviewLightDark() {
    SevTheme {
        Column {
            RoundedSearchBar(
                state = TopBarState.Search("", false),
                onSearchTermChanged = {},
                onOpenSearchScreen = {},
                onCancelClicked = {},
            )
            Spacer(Modifier.height(8.dp))
            RoundedSearchBar(
                state = TopBarState.Search("", true),
                onSearchTermChanged = {},
                onOpenSearchScreen = {},
                onCancelClicked = {},
            )
        }
    }
}

@PreviewLightDark()
@Composable
internal fun SearchStopPreviewLightDark() {
    SevTheme {
        RoundedSearchBar(
            state = TopBarState.StopSelected(Stubs.stops[0]),
            onSearchTermChanged = {},
            onOpenSearchScreen = {},
            onCancelClicked = {},
        )
    }
}

@PreviewLightDark()
@Composable
internal fun SearchLinePreviewLightDark() {
    SevTheme {
        RoundedSearchBar(
            state = TopBarState.LineSelected(Stubs.lines[0]),
            onSearchTermChanged = {},
            onOpenSearchScreen = {},
            onCancelClicked = {},
        )
    }
}

