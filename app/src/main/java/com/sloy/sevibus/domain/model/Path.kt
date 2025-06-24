package com.sloy.sevibus.domain.model

/**
 * Checksum omitted from the domain model, but used in the database and API models
 */
typealias PathChecksum = String

data class Path(
    val routeId: RouteId,
    val points: List<Position>,
    val line: LineSummary,
)

fun Position.moveToPath(path: Path): Position {
    return path.points.minBy { this.manhattanDistance(it) }
}

fun Stop.moveToPath(path: Path): Stop {
    return copy(position = position.moveToPath(path))
}

fun List<Stop>.moveStopsToPath(path: Path?): List<Stop> {
    if (path == null) return this
    return this.map { stop -> stop.moveToPath(path) }
}


/**
 * Moves the points of a path diagonally by a factor.
 * The path is supposed to be part of a list of multiple paths, so they don't intersect after nudged.
 * The nudge factor depends on the index of the path in the list and the total number of paths, so that some paths
 * are moved in one direction and some in the opposite.
 *
 * For example, if there are 4 paths total they will be nudged by -2x, -x, 0, +x respectively.
 */
fun Path.nudgeBy(index: Int, total: Int): Path {
    val factor = -1 * total / 2 + index
    // Increment calculated by the exact science of trial and error
    val latIncrement = -0.00001 * factor
    val lonIncrement = 0.00005 * factor
    val newPoints = points.map {
        Position(it.latitude + latIncrement, it.longitude + lonIncrement)
    }
    return Path(routeId, newPoints, line)
}

/**
 * Uses [nudgeBy] to avoid collision between paths in a list.
 */
fun List<Path>.nudge(): List<Path> {
    return this.mapIndexed { index, path ->
        path.nudgeBy(index, size)
    }
}

/**
 * Uses [nudgeBy] to avoid collistion between paths of different colors.
 * The difference with [nudge] is that this function won't split every single Path, instead will do it in groups of colors.
 * So that red lines are moved to one side, blue lines to the other side, and so on.
 * Helpful when there are too many paths to nudge everything.
 */
fun List<Path>.nudgeByColors(): List<Path> {
    val colors = this.map { it.line.color }.distinct()
    return this.map { path ->
        val colorIndex = colors.indexOf(path.line.color)
        path.nudgeBy(colorIndex, colors.size)
    }
}

fun Path.splitBy(position: Position): Pair<List<Position>, List<Position>> {
    if (points.first() == position) {
        return Pair(emptyList(), points)
    }
    if (points.last() == position) {
        return Pair(points, emptyList())
    }
    val splitPointIndex = points.indexOf(position)
    val path1 = points.subList(0, splitPointIndex + 1)
    val path2 = points.subList(splitPointIndex, points.size)
    return Pair(path1, path2)
}
