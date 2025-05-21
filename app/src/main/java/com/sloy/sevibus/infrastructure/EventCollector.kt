package com.sloy.sevibus.infrastructure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun <T> EventCollector(flow: Flow<T>, collector: suspend (T) -> Unit) {
    LaunchedEffect(Unit) {
        flow.collect { collector(it) }
    }

}
