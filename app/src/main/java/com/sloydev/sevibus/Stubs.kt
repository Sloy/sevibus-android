package com.sloydev.sevibus

import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Work
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.FavoriteStop
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineColor
import com.sloydev.sevibus.domain.model.Position
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RouteWithStops
import com.sloydev.sevibus.domain.model.SearchResult
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.TravelCard
import com.sloydev.sevibus.domain.model.toSummary
import com.sloydev.sevibus.feature.lines.LinesScreenState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@Deprecated("")
object Stubs {

    @Deprecated("")
    val routes: List<Route> = listOf(
        Route(
            id = "1.1",
            direction = 1,
            destination = "Heliopolis",
            line = 1,
            stops = listOf(1, 2, 3, 4, 5),
        ),
        Route(
            id = "1.2",
            direction = 2,
            destination = "Puerta Triana",
            line = 1,
            stops = listOf(10, 9, 8, 7, 6),
        ),
    )

    @Deprecated("")
    val lines = listOf(
        Line("01", "Plg. Norte H. Virgen del Rocio", LineColor.Red, routes = routes, id = 1),
        Line("02", "Puerta Triana - Heliopolis", LineColor.Red, routes = routes, id = 2),
        Line("03", "Bellavista-S. Jeronimo-Pino Montano", LineColor.Orange, routes = routes, id = 3),
        Line("05", "Puerta Triana - Santa Aurelia", LineColor.Red, routes = routes, id = 5),
        Line("06", "Gta. S. Lázaro - Hosp. V. Rocio", LineColor.Red, routes = routes, id = 6),
        Line("10", "Ponce de León - San Jerónimo", LineColor.Blue, routes = routes, id = 10),
        Line("11", "Ponce de León - Los Principes", LineColor.Blue, routes = routes, id = 11),
        Line("12", "Ponce de León -  Pino Montano", LineColor.Blue, routes = routes, id = 12),
        Line("13", "Plaza Duque Pino Montano", LineColor.Blue, routes = routes, id = 13),
        Line("14", "Plaza Duque - Poligono Norte - Las Golondrinas", LineColor.Blue, routes = routes, id = 14),
        Line("15", "Ponce de León - San Diego", LineColor.Blue, routes = routes, id = 15),
        Line("16", "Rialto - Valdezorras", LineColor.Blue, routes = routes, id = 16),
        Line("20", "Ponce de León - Polígono de San Pablo", LineColor.Blue, routes = routes, id = 20),
        Line("21", "Plaza de Armas - Polígono San Pablo", LineColor.Blue, routes = routes, id = 21),
        Line("22", "Prado San Sebastian - Sevilla Este", LineColor.Blue, routes = routes, id = 22),
        Line("24", "Ponce de León - Juan XXIII - Palmete", LineColor.Blue, routes = routes, id = 24),
        Line("25", "Prado - Rochelambert", LineColor.Blue, routes = routes, id = 25),
        Line("26", "Prado - Cerro del Aguila", LineColor.Blue, routes = routes, id = 26),
        Line("27", "Plaza Duque - Sevilla Este", LineColor.Blue, routes = routes, id = 27),
        Line("28", "Prado - Alcosa", LineColor.Blue, routes = routes, id = 28),
        Line("29", "Prado S. Sebastian - Torreblanca", LineColor.Blue, routes = routes, id = 29),
        Line("30", "Prado San Sebastián - La Paz", LineColor.Blue, routes = routes, id = 30),
        Line("31", "Prado San Sebastián - Polígono Sur", LineColor.Blue, routes = routes, id = 31),
        Line("32", "Plz. Duque - Plg. Sur", LineColor.Blue, routes = routes, id = 32),
        Line("34", "Prado San Sebastián - Los Bermejales", LineColor.Blue, routes = routes, id = 34),
        Line("37", "Pta. Jerez - Pedro Salvador - Bellavista", LineColor.Blue, routes = routes, id = 37),
        Line("38A", "Prado - Pitamo - Olavide", LineColor.Blue, routes = routes, id = 38),
        Line("39", "Los Arcos - Hac. S. Antonio", LineColor.Blue, routes = routes, id = 39),
        Line("40", "Reyes Catolicos - Triana", LineColor.Blue, routes = routes, id = 40),
        Line("41", "Reyes Catolicos - Tablada", LineColor.Blue, routes = routes, id = 41),
        Line("43", "San Pablo - El Tardon", LineColor.Blue, routes = routes, id = 43),
        Line("52", "S. Bernardo - Palmete", LineColor.Blue, routes = routes, id = 52),
        Line("B3", "Gran Plaza - Santa Clara", LineColor.Cyan, routes = routes, id = 83),
        Line("B4", "San Bernardo - Torreblanca", LineColor.Cyan, routes = routes, id = 84),
        Line("C1", "Circular Exterior 1", LineColor.Green, routes = routes, id = 1001),
        Line("C2", "Circular Exterior 2", LineColor.Green, routes = routes, id = 1002),
        Line("C3", "Circular Interior 1", LineColor.Green, routes = routes, id = 1003),
        Line("C4", "Circular Interior 2", LineColor.Green, routes = routes, id = 1004),
        Line("C6A", "Circular Macarena Norte Sentido A", LineColor.Green, routes = routes, id = 1006),
        Line("C6B", "Circular Macarena Norte Sentido B", LineColor.Green, routes = routes, id = 1007),
        Line("CJ", "San Bernardo - Ciudad de La Justicia", LineColor.Orange, routes = routes, id = 100),
        Line("EC", "Lanzadera Charco La Pava", LineColor.Cyan, routes = routes, id = 101),
        Line("LE", "Prado San Sebastián - Sevilla Este", LineColor.Orange, routes = routes, id = 102),
        Line("LN", "Line Norte", LineColor.Orange, routes = routes, id = 103),
        Line("LS", "Santa Justa - Bellavista", LineColor.Orange, routes = routes, id = 104),
        Line("T1", "Metrocentro", LineColor.Wine, routes = routes, id = 105),
    )

    @Deprecated("")
    val stops = listOf(
        Stop(
            code = 430,
            description = "Glorieta Plus Ultra",
            position = Position(latitude = 37.356990, longitude = -5.980317),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 566,
            description = "Manuel Siurot (Rafael Salgado)",
            position = Position(latitude = 37.359176, longitude = -5.980631),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 154,
            description = "Manuel Siurot (Hospital V.Rocio)",
            position = Position(latitude = 37.361072, longitude = -5.981739),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 64,
            description = "Manuel Siurot (Gustavo Gallardo)",
            position = Position(latitude = 37.365230, longitude = -5.983750),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 65,
            description = "Manuel Siurot (Edificio La Estrella)",
            position = Position(latitude = 37.367492, longitude = -5.985054),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 66,
            description = "Avenida La Borbolla (Felipe Ii)",
            position = Position(latitude = 37.372348, longitude = -5.985392),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 67,
            description = "Avenida La Borbolla (Montevideo)",
            position = Position(latitude = 37.374198, longitude = -5.985209),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 68,
            description = "Avda. La Borbolla (Dr. Pedro de Castro)",
            position = Position(latitude = 37.379100, longitude = -5.983300),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 472,
            description = "Menendez Pelayo (Juzgados)",
            position = Position(latitude = 37.381877, longitude = -5.987963),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 42,
            description = "Menéndez Pelayo (Puerta de La Carne)",
            position = Position(latitude = 37.386516, longitude = -5.985553),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 43,
            description = "Recaredo (Puerta Carmona)",
            position = Position(latitude = 37.389663, longitude = -5.984265),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 888,
            description = "Recaredo (San Roque)",
            position = Position(latitude = 37.391544, longitude = -5.984123),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 22,
            description = "María Auxiliadora (Puerta Osario)",
            position = Position(latitude = 37.394493, longitude = -5.983595),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 23,
            description = "Ronda de Capuchinos (Avenida Miraflores)",
            position = Position(latitude = 37.398807, longitude = -5.981822),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 24,
            description = "Ronda de Capuchinos (León XIII)",
            position = Position(latitude = 37.399669, longitude = -5.983595),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 25,
            description = "Muñoz León (Sánchez Perrier)",
            position = Position(latitude = 37.401397, longitude = -5.985486),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 238,
            description = "San Juan de Ribera (Macarena)",
            position = Position(latitude = 37.403549, longitude = -5.987237),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 239,
            description = "San Juan de Ribera (Policlínico)",
            position = Position(latitude = 37.405429, longitude = -5.986314),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 233,
            description = "Doctor Fedriani (Doctor Leal Castaños)",
            position = Position(latitude = 37.407817, longitude = -5.985156),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 251,
            description = "Doctor Fedriani (Plaza Isla Canela)",
            position = Position(latitude = 37.409427, longitude = -5.984575),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        ),
        Stop(
            code = 252,
            description = "Doctor Fedriani (Avda. San Lázaro)",
            position = Position(latitude = 37.411148, longitude = -5.983941),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary() },
        )
    )

    @Deprecated("")
    val favorites = listOf(
        FavoriteStop(customName = "Casa", icon = Icons.Rounded.Home, stop = stops[0]),
        FavoriteStop(customName = "Oficina", icon = Icons.Rounded.Work, stop = stops[1]),
    )

    @Deprecated("")
    val groupsOfLines = lines
        .groupBy { it.group }
        .map { (group, linesForGroup) -> LinesScreenState.Content.GroupOfLines(group, linesForGroup) }

    @Deprecated("")
    val lineGroups = listOf(
        "Circulares",
        "Transversales",
        "Radiales Norte",
        "Radiales Este",
        "Radiales Sur",
        "Radiales Oeste",
        "Periféricas",
        "Barrios y Express",
        "Especiales",
        "Metroentro",
        "Nocturnas",
        "Otras",
    )

    @Deprecated("")
    val routesWithStops: List<RouteWithStops> = routes.map {
        RouteWithStops(it, stops.shuffled())
    }

    @Deprecated("")
    val cardWithAllFields = TravelCard(
        code = 30,
        title = "Full test",
        customName = "Completo",
        balanceMillis = 8450,
        balanceTrips = 42,
        serialNumber = 794836666,
        expiration = LocalDate.now(),
        validityEnd = LocalDate.now(),
        extensionEnd = LocalDate.now()
    )

    @Deprecated("")
    val cards = listOf(
        TravelCard(
            code = 31,
            title = "Saldo sin transbordo",
            customName = "Bono Laura",
            balanceMillis = 8450,
            serialNumber = randomCardSerial()
        ),
        TravelCard(
            code = 70,
            title = "Hijo de empleado",
            customName = "La de paco",
            balanceTrips = 35,
            serialNumber = randomCardSerial()
        ),
        TravelCard(
            code = 116,
            title = "Estudiante mensual",
            customName = "Estudiante",
            validityEnd = LocalDate.now(),
            extensionEnd = LocalDate.now(),
            serialNumber = randomCardSerial()
        ),
        TravelCard(
            code = 30,
            title = "Saldo con transbordo",
            customName = "Números rojos",
            balanceMillis = -450,
            serialNumber = randomCardSerial()
        ),
    )

    private fun randomCardSerial() = Random.nextLong(100000000, 999999999)

    @Deprecated("")
    val travelCardTransactions = listOf(
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[4], people = 2),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[4]),
        TravelCard.Transaction.TopUp(10000, LocalDateTime.now()),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[34], people = 2),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[34]),
    )

    @Deprecated("")
    val searchResults: List<SearchResult>
        get() {
            val stops = stops.shuffled().take(20)
                .map { SearchResult.StopResult(it) }
            val lines = lines.shuffled().take(10)
                .map { SearchResult.LineResult(it) }
            return (stops + lines).shuffled()
        }

    @Deprecated("")
    fun groupFromLine(label: String): String {
        return when (label) {
            "C1",
            "C2",
            "C3",
            "C4",
            "C6A",
            "C6B" -> "Circulares"

            "01",
            "02",
            "03",
            "05",
            "06" -> "Transversales"

            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16" -> "Radiales Norte"

            "20",
            "21",
            "22",
            "24",
            "25",
            "26",
            "27",
            "28",
            "29" -> "Radiales Este"

            "30",
            "31",
            "32",
            "34",
            "37",
            "38",
            "38A",
            "39" -> "Radiales Sur"

            "40",
            "41",
            "43" -> "Radiales Oeste"

            "52",
            "53" -> "Periféricas"

            "B3",
            "B4",
            "CJ",
            "EA",
            "LE",
            "LN",
            "LS" -> "Barrios y Express"

            "E0",
            "EC" -> "Especiales"

            "T1" -> "Metroentro"

            "A1",
            "A2",
            "A3",
            "A4",
            "A5",
            "A6",
            "A7",
            "A8" -> "Nocturnas"

            else -> "Otras"
        }
    }

    @Deprecated("Be careful using Stubs")
    val arrivals = listOf(
        BusArrival(bus = 1, distance = 50, seconds = 0, line = lines.random().toSummary(), route = routes.random()),
        BusArrival(bus = 6, distance = 50, seconds = 2 * 60, line = lines.random().toSummary(), route = routes.random()),
        BusArrival(bus = 2, distance = 50, seconds = 5 * 60, line = lines.random().toSummary(), route = routes.random()),
        BusArrival(bus = 3, distance = 100, seconds = 10 * 60, line = lines.random().toSummary(), route = routes.random()),
        BusArrival(bus = 8, distance = 100, seconds = 15 * 60, line = lines.random().toSummary(), route = routes.random()),
        BusArrival(bus = 4, distance = 500, seconds = null, line = lines.random().toSummary(), route = routes.random()),
    )

    suspend inline fun delayNetwork(): Unit = delay(1_000)

    @Deprecated("Be careful using Stubs")
    val locationTriana = LatLng(37.385222, -6.011210)

    @Deprecated("Be careful using Stubs")
    val locationRecaredo = LatLng(37.389083, -5.984483)

    @Deprecated("Be careful using Stubs")
    val locationSource = FakeLocationSource()
}

class FakeLocationSource : LocationSource {

    private var listener: LocationSource.OnLocationChangedListener? = null

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
        listener.onLocationChanged(Location("FakeLocationSource").apply {
            latitude = Stubs.locationTriana.latitude
            longitude = Stubs.locationTriana.longitude
        })
    }

    override fun deactivate() {
        listener = null
    }

}

