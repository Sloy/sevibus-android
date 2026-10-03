package com.sloy.debugmenu.overlay

internal enum class Edge { LEFT, RIGHT }

internal fun snapX(edge: Edge, buttonSize: Float, screenWidth: Float): Float = when (edge) {
    Edge.LEFT -> -buttonSize / 3f
    Edge.RIGHT -> screenWidth - buttonSize * 2f / 3f
}

internal fun hiddenX(edge: Edge, buttonSize: Float, screenWidth: Float): Float = when (edge) {
    Edge.LEFT -> -buttonSize
    Edge.RIGHT -> screenWidth
}

internal fun nearestEdge(x: Float, buttonSize: Float, screenWidth: Float): Edge =
    if (x + buttonSize / 2f >= screenWidth / 2f) Edge.RIGHT else Edge.LEFT

internal fun clampY(y: Float, buttonSize: Float, minY: Float, maxY: Float): Float =
    y.coerceIn(minY, (maxY - buttonSize).coerceAtLeast(minY))
