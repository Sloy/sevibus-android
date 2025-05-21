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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemShimmer
import com.sloy.sevibus.ui.theme.SevTheme
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppLayoutPlayground(modifier: Modifier = Modifier) {
    SevTheme {

        val insetPaddings = WindowInsets.systemBars.asPaddingValues()
        val bottomInsetPadding by remember { derivedStateOf { insetPaddings.calculateBottomPadding() } }
        val topInsetPadding by remember { derivedStateOf { insetPaddings.calculateTopPadding() } }

        var isBottomBarVisible by remember { mutableStateOf(false) }
        val bottomBarHeight by remember { derivedStateOf{ if (isBottomBarVisible) 80.dp else 0.dp }}

        val sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded,
            defineValues = {
                SevLogger.logPotato("bottomBarHeight: $bottomBarHeight")
                SevLogger.logPotato("bottomInsetPadding: $bottomInsetPadding")
                SheetValue.Hidden at height(bottomBarHeight + bottomInsetPadding + 48.dp)
                SheetValue.PartiallyExpanded at offset(percent = 40)
                SheetValue.Expanded at offset(topInsetPadding) //contentHeight// offset(topInsetPadding)
            })
        val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

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
            }
        ) { scaffoldPadding ->

            SevLogger.logPotato("scaffoldPadding: $scaffoldPadding")
            BottomSheetScaffold(
                topBar = {
                    Box(
                        Modifier
                            .systemBarsPadding()
                            .background(Color.Blue.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "Top Bar", textAlign = TextAlign.Center, modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                },
                scaffoldState = scaffoldState,
                sheetContent = {
                    Column(
                        Modifier
                            .navigationBarsPadding()
                            .consumeWindowInsets(PaddingValues(bottom = bottomBarHeight))
                    ) {
                        Box(
                            Modifier
                                .border(2.dp, Color.Blue)
                                .weight(1f)
                        ) {
                            // <sheetContent>
                            Column(
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth()
                            ) {
                                repeat(20) {
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
                            }
                            // </sheetContent>

                        }
                        Box(Modifier.height(topInsetPadding)) // <-- Trick to overcome the offset on the Expanded bottomsheet top
                        Box(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
                        Box(Modifier
                            .height(bottomBarHeight)
                            .animateContentSize())
                    }
                },
                content = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(2.dp, Color.Red)
                            .background(Color.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()

                        ) {
                            Button(onClick = {
                                isBottomBarVisible = !isBottomBarVisible
                                sheetState.refreshValues()
                            }, Modifier.safeDrawingPadding()) {
                                Text("Bottom bar visible")
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
                        }

                    }
                }
            )

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppLayoutPlayground()
}
