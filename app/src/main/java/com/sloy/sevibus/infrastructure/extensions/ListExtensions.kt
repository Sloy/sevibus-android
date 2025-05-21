package com.sloy.sevibus.infrastructure.extensions

internal fun <T> List<T>.swap(from: Int, to: Int): List<T> {
    return toMutableList().apply {
        add(to, removeAt(from))
    }
}
