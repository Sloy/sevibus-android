package com.sloy.sevibus.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.FavoriteStopDto
import com.sloy.sevibus.data.api.model.LineDto
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.api.model.RouteDto
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.PathChecksum
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Polyline
import com.sloy.sevibus.infrastructure.polyline.toPositions
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import java.time.LocalTime

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey
    val code: StopId,
    val description: String,
    @Embedded
    val position: Position,
    val lines: List<LineId>,
    val searchableText: String = "${code.toString()} $description",
)

@Entity(tableName = "stops_fts", primaryKeys = ["rowid"])
@Fts4(contentEntity = StopEntity::class, tokenizer = "unicode61", tokenizerArgs = ["remove_diacritics=1"])
data class StopFtsEntity(
    @ColumnInfo(name = "rowid") val code: Int,
    val searchableText: String,
)

@Entity(tableName = "lines")
data class LineEntity(
    @PrimaryKey
    val id: LineId,
    val label: String,
    val description: String,
    val color: LineColor,
    val group: String,
    val routes: List<RouteId>,
    val searchableText: String = "$label $description $group",
)

@Entity(tableName = "lines_fts", primaryKeys = ["rowid"])
@Fts4(contentEntity = LineEntity::class, tokenizer = "unicode61", tokenizerArgs = ["remove_diacritics=1"])
data class LineFtsEntity(
    @ColumnInfo(name = "rowid") val code: Int,
    val searchableText: String,
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    @Embedded
    val schedule: Route.Schedule,
    val stops: List<StopId>,
)

@Entity(tableName = "paths")
data class PathEntity(
    @PrimaryKey val routeId: RouteId,
    val polyline: Polyline,
    @ColumnInfo(defaultValue = "")
    val checksum: PathChecksum
)

@Entity(tableName = "favorites")
data class FavoriteStopEntity(
    @PrimaryKey val stopId: StopId,
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val order: Int = 0,
)

@Entity(tableName = "cards")
data class CardInfoEntity(
    @PrimaryKey val serialNumber: CardId,
    val code: Int,
    val type: String,
    val balance: Int? = null,
    val customName: String? = null,
    val order: Int = 0,
)

fun LineEntity.fromEntity(routes: List<Route>): Line {
    return Line(label, description, color, group, id = id, routes = routes)
}

fun LineEntity.summaryFromEntity(): LineSummary = LineSummary(id, label, color)
fun Line.toEntity(): LineEntity {
    return LineEntity(id, label, description, color, group, routes.map { it.id })
}


fun Route.toEntity(): RouteEntity {
    return RouteEntity(
        id, direction, destination, line, schedule, stops
//        RouteEntity.Schedule(schedule.startTime, schedule.endTime)
    )
}

fun RouteEntity.fromEntity(): Route {
    return Route(id, direction, destination, line, stops, schedule)
}

fun PathEntity.fromEntity(line: LineSummary): Path {
    return Path(routeId, polyline.toPositions(), line)
}

fun PathDto.fromDto(line: LineSummary): Path {
    return Path(routeId, polyline.toPositions(), line)
}

fun PathDto.toEntity(): PathEntity {
    return PathEntity(routeId, polyline, checksum)
}

fun RouteDto.fromEntity(): RouteEntity {
    return RouteEntity(id, direction, destination, line, schedule.fromDto(), stops)
}

private fun RouteDto.ScheduleDto.fromDto(): Route.Schedule {
    return Route.Schedule(
        startTime = LocalTime.parse(this.startTime),
        endTime = LocalTime.parse(this.endTime),
    )
}

fun LineDto.toEntity(): LineEntity {
    return LineEntity(id, label, description, color, group, routes)
}

fun FavoriteStop.toEntity(order: Int = 0): FavoriteStopEntity {
    return FavoriteStopEntity(
        stopId = stop.code,
        customName = customName,
        customIcon = customIcon,
        order = order
    )
}

fun FavoriteStopDto.toEntity(): FavoriteStopEntity {
    return FavoriteStopEntity(
        stopId = stopId,
        customName = customName,
        customIcon = customIcon,
        order = order
    )
}

fun FavoriteStopEntity.toDto(): FavoriteStopDto {
    return FavoriteStopDto(
        stopId = stopId,
        customName = customName,
        customIcon = customIcon,
        order = order
    )
}

fun CardInfoEntity.fromEntity(): CardInfo {
    return CardInfo(
        serialNumber = serialNumber,
        code = code,
        type = type,
        balance = balance,
        customName = customName,
    )
}

fun CardInfo.toEntity(order: Int): CardInfoEntity {
    return CardInfoEntity(
        serialNumber = serialNumber,
        code = code,
        type = type,
        balance = balance,
        customName = customName,
        order = order,
    )
}

fun CardInfoEntity.toDto(): CardInfoDto {
    return CardInfoDto(
        serialNumber = serialNumber,
        code = code,
        type = type,
        balance = balance,
        customName = customName,
        order = order,
    )
}

fun CardInfoDto.toEntity(): CardInfoEntity {
    return CardInfoEntity(
        serialNumber = serialNumber,
        code = code,
        type = type,
        balance = balance,
        customName = customName,
        order = order,
    )
}
