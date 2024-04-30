package com.sloydev.sevibus.domain.model

data class Path(
    val routeId: RouteId,
    val points: List<Position>
)

fun Position.moveToPath(path: Path): Position {
    return path.points.minBy { this.manhattanDistance(it) }
}
