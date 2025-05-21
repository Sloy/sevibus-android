package com.sloy.sevibus.infrastructure

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

fun ticker(delay: Duration): Flow<Unit> = ticker(delay.inWholeMilliseconds)

fun ticker(delayMillis: Long): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(delayMillis)
    }
}
