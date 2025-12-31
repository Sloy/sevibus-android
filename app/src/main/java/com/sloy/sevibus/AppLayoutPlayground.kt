package com.sloy.sevibus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.DragIndication
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemShimmer
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppLayoutPlayground(modifier: Modifier = Modifier) {
    SevTheme {

        val scope = rememberCoroutineScope()

        val systemBarInsetPaddings = WindowInsets.systemBars.asPaddingValues()
        val bottomInsetPadding by remember { derivedStateOf { systemBarInsetPaddings.calculateBottomPadding() } }
        val topInsetPadding by remember { derivedStateOf { systemBarInsetPaddings.calculateTopPadding() } }

        var isBottomBarVisible by remember { mutableStateOf(true) }
        val bottomBarHeight by remember { derivedStateOf { if (isBottomBarVisible) 80.dp else 0.dp } }

        val CollapsedWithBottomBar = SheetDetent("CollapsedWithBottomBar") { containerHeight, sheetHeight ->
            // bottomBarHeight + bottomInsetPadding + SHEET_DRAG_HANDLE_HEIGHT + screenPeekingHeight
            bottomBarHeight + bottomInsetPadding + DRAG_INDICATOR_HEIGHT
        }

        val PartiallyCollapsed = SheetDetent("PartiallyCollapsed") { containerHeight, sheetHeight ->
            containerHeight * 0.4f
        }
        val PartiallyExpanded = SheetDetent("PartiallyExpanded") { containerHeight, sheetHeight ->
            containerHeight * 0.7f
        }
        val Expanded = SheetDetent("Expanded") { containerHeight, sheetHeight ->
            containerHeight - topInsetPadding
        }

        val sheetState = rememberBottomSheetState(
            initialDetent = Expanded,
            detents = listOf(CollapsedWithBottomBar, PartiallyCollapsed, PartiallyExpanded, Expanded),
        )


        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    Box(
                        Modifier
                            .systemBarsPadding()
                            .height(80.0.dp)
                            .background(Color.Blue.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "Bottom Bar", textAlign = TextAlign.Center, modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            },
            content = { paddingValues ->
                MapContainer(sheetState, onToggleBottomBar = {
                    scope.launch {
                        val currentDetent = sheetState.targetDetent
                        sheetState.animateTo(CollapsedWithBottomBar)
                        isBottomBarVisible = !isBottomBarVisible
                        sheetState.invalidateDetents()
                        sheetState.animateTo(currentDetent)
                    }
                })
                BottomSheet(
                    state = sheetState,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(SevTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = topInsetPadding), // To compensate the top padding
                    ) {
                        DragIndication(
                            modifier = Modifier
                                .padding(vertical = DRAG_INDICATOR_PADDING)
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(100))
                                .width(32.dp)
                                .height(DRAG_INDICATOR_HANDLE_HEIGHT)
                                .align(Alignment.CenterHorizontally),
                        )
                        // Sheet Content
                        SheetContent(bottomBarHeight, bottomInsetPadding, topInsetPadding)
                    }
                }
            }
        )

    }
}



@Composable
private fun SheetContent(bottomBarHeight: Dp, bottomInsetPadding: Dp, topInsetPadding: Dp) {
    Column(
        Modifier
            .navigationBarsPadding()
            .consumeWindowInsets(PaddingValues(bottom = bottomBarHeight))
    ) {
        Box(
            Modifier
                .border(2.dp, Color.Cyan)
                .weight(1f)
        ) {
            // <sheetContent>
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                repeat(10) {
                    val scope = rememberCoroutineScope()
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    FavoriteListItemShimmer()
                    TextField(
                        value = "",
                        onValueChange = {},
                        Modifier
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .padding(16.dp)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    scope.launch {
                                        delay(100)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                }
                Text("THE END")
            }
            // </sheetContent>

        }
        Box(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        Box(
            Modifier
                .height(bottomBarHeight)
                .animateContentSize()
        )
    }
}

@Composable
private fun MapContainer(sheetState: BottomSheetState, onToggleBottomBar: () -> Unit) {
    // Map Background
    Box(
        Modifier
            .fillMaxSize()
            .border(2.dp, Color.Red)
            .background(Color.Green),
        contentAlignment = Alignment.Center
    ) {
        with(LocalDensity.current) {
            // MapScreen
            val topSystemBarPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = (sheetState.offset.toDp() - topSystemBarPadding).coerceAtLeast(0.dp))
                    .padding(top = topSystemBarPadding)
                    .border(4.dp, Color.Blue)
                    .background(Color.Blue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    onToggleBottomBar()
                }) {
                    Text("Toggle bottom bar")
                }
                //FullScreenContent()
            }
        }

    }
}

@Composable
private fun FullScreenContent() {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()

    ) {
        Button(onClick = {
            //sheetState.refreshValues()
        }) {
            Text("Toggle bottom bar")
        }
        repeat(20) {
            val scope = rememberCoroutineScope()
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            FavoriteListItemShimmer()
            TextField(
                value = "",
                onValueChange = {},
                Modifier
                    .padding(16.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            scope.launch {
                                delay(1000)
                                //bringIntoViewRequester.bringIntoView()
                            }
                        }
                    }
            )
        }
        Text("THE END")
    }
}

@Preview(showSystemUi = true)
@Composable
internal fun Preview() {
    AppLayoutPlayground()
}
