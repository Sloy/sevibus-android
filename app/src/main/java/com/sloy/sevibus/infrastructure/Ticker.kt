package com.sloy.sevibus.infrastructure

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun ticker(delayMillis: Long): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(delayMillis)
    }
}
