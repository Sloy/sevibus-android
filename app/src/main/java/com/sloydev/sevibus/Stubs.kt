package com.sloydev.sevibus

import com.sloydev.sevibus.domain.model.Line
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

object Stubs {


    val routes: List<Route> = listOf(
        Route(
            id = "1.1",
            direction = 1,
            destination = "HOSPITAL V.ROCIO",
            line = 1,
            stops = listOf(1, 2, 3, 4, 5),
        ),
        Route(
            id = "1.2",
            direction = 2,
            destination = "POLIGONO NORTE",
            line = 1,
            stops = listOf(10, 9, 8, 7, 6),
        ),
    )

    val lines = listOf(
        Line("01", "Plg. Norte H. Virgen del Rocio", 0xffF54129, routes = routes, id=1),
        Line("02", "Puerta Triana - Heliopolis", 0xffF54129, routes = routes, id=2),
        Line("03", "Bellavista-S. Jeronimo-Pino Montano", 0xffF7A800, routes = routes, id=3),
        Line("05", "Puerta Triana - Santa Aurelia", 0xffF54129, routes = routes, id=5),
        Line("06", "Gta. S. Lázaro - Hosp. V. Rocio", 0xffF54129, routes = routes, id=6),
        Line("10", "Ponce de León - San Jerónimo", 0xff000D6F, routes = routes, id=10),
        Line("11", "Ponce de León - Los Principes", 0xff000D6F, routes = routes, id=11),
        Line("12", "Ponce de León -  Pino Montano", 0xff000D6F, routes = routes, id=12),
        Line("13", "Plaza Duque Pino Montano", 0xff000D6F, routes = routes, id=13),
        Line("14", "Plaza Duque - Poligono Norte - Las Golondrinas", 0xff000D6F, routes = routes, id=14),
        Line("15", "Ponce de León - San Diego", 0xff000D6F, routes = routes, id=15),
        Line("16", "Rialto - Valdezorras", 0xff000D6F, routes = routes, id=16),
        Line("20", "Ponce de León - Polígono de San Pablo", 0xff000D6F, routes = routes, id=20),
        Line("21", "Plaza de Armas - Polígono San Pablo", 0xff000D6F, routes = routes, id=21),
        Line("22", "Prado San Sebastian - Sevilla Este", 0xff000D6F, routes = routes, id=22),
        Line("24", "Ponce de León - Juan XXIII - Palmete", 0xff000D6F, routes = routes, id=24),
        Line("25", "Prado - Rochelambert", 0xff000D6F, routes = routes, id=25),
        Line("26", "Prado - Cerro del Aguila", 0xff000D6F, routes = routes, id=26),
        Line("27", "Plaza Duque - Sevilla Este", 0xff000D6F, routes = routes, id=27),
        Line("28", "Prado - Alcosa", 0xff000D6F, routes = routes, id=28),
        Line("29", "Prado S. Sebastian - Torreblanca", 0xff000D6F, routes = routes, id=29),
        Line("30", "Prado San Sebastián - La Paz", 0xff000D6F, routes = routes, id=30),
        Line("31", "Prado San Sebastián - Polígono Sur", 0xff000D6F, routes = routes, id=31),
        Line("32", "Plz. Duque - Plg. Sur", 0xff000D6F, routes = routes, id=32),
        Line("34", "Prado San Sebastián - Los Bermejales", 0xff000D6F, routes = routes, id=34),
        Line("37", "Pta. Jerez - Pedro Salvador - Bellavista", 0xff000D6F, routes = routes, id=37),
        Line("38A", "Prado - Pitamo - Olavide", 0xff000D6F, routes = routes, id=38),
        Line("39", "Los Arcos - Hac. S. Antonio", 0xff000D6F, routes = routes, id=39),
        Line("40", "Reyes Catolicos - Triana", 0xff000D6F, routes = routes, id=40),
        Line("41", "Reyes Catolicos - Tablada", 0xff000D6F, routes = routes, id=41),
        Line("43", "San Pablo - El Tardon", 0xff000D6F, routes = routes, id=43),
        Line("52", "S. Bernardo - Palmete", 0xff000D6F, routes = routes, id=52),
        Line("B3", "Gran Plaza - Santa Clara", 0xff84C6E3, routes = routes, id=83),
        Line("B4", "San Bernardo - Torreblanca", 0xff84C6E3, routes = routes, id=84),
        Line("C1", "Circular Exterior 1", 0xff008431, routes = routes, id=1001),
        Line("C2", "Circular Exterior 2", 0xff008431, routes = routes, id=1002),
        Line("C3", "Circular Interior 1", 0xff008431, routes = routes, id=1003),
        Line("C4", "Circular Interior 2", 0xff008431, routes = routes, id=1004),
        Line("C6A", "Circular Macarena Norte Sentido A", 0xff008431, routes = routes, id=1006),
        Line("C6B", "Circular Macarena Norte Sentido B", 0xff008431, routes = routes, id=1007),
        Line("CJ", "San Bernardo - Ciudad de La Justicia", 0xffF7A800, routes = routes, id=100),
        Line("EC", "Lanzadera Charco La Pava", 0xff84C6E3, routes = routes, id=101),
        Line("LE", "Prado San Sebastián - Sevilla Este", 0xffF7A800, routes = routes, id=102),
        Line("LN", "Line Norte", 0xffF7A800, routes = routes, id=103),
        Line("LS", "Santa Justa - Bellavista", 0xffF7A800, routes = routes, id=104),
        Line("T1", "Metrocentro", 0xffC60018, routes = routes, id=105),
    )

    val stops = listOf(
        Stop(
            code = 430,
            description = "Glorieta Plus Ultra",
            position = Stop.Position(latitude = 37.356990, longitude = -5.980317),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 566,
            description = "Manuel Siurot (Rafael Salgado)",
            position = Stop.Position(latitude = 37.359176, longitude = -5.980631),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 154,
            description = "Manuel Siurot (Hospital V.Rocio)",
            position = Stop.Position(latitude = 37.361072, longitude = -5.981739),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 64,
            description = "Manuel Siurot (Gustavo Gallardo)",
            position = Stop.Position(latitude = 37.365230, longitude = -5.983750),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 65,
            description = "Manuel Siurot (Edificio La Estrella)",
            position = Stop.Position(latitude = 37.367492, longitude = -5.985054),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 66,
            description = "Avenida La Borbolla (Felipe Ii)",
            position = Stop.Position(latitude = 37.372348, longitude = -5.985392),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 67,
            description = "Avenida La Borbolla (Montevideo)",
            position = Stop.Position(latitude = 37.374198, longitude = -5.985209),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 68,
            description = "Avda. La Borbolla (Dr. Pedro de Castro)",
            position = Stop.Position(latitude = 37.379100, longitude = -5.983300),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 472,
            description = "Menendez Pelayo (Juzgados)",
            position = Stop.Position(latitude = 37.381877, longitude = -5.987963),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 42,
            description = "Menéndez Pelayo (Puerta de La Carne)",
            position = Stop.Position(latitude = 37.386516, longitude = -5.985553),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 43,
            description = "Recaredo (Puerta Carmona)",
            position = Stop.Position(latitude = 37.389663, longitude = -5.984265),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 888,
            description = "Recaredo (San Roque)",
            position = Stop.Position(latitude = 37.391544, longitude = -5.984123),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 22,
            description = "María Auxiliadora (Puerta Osario)",
            position = Stop.Position(latitude = 37.394493, longitude = -5.983595),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 23,
            description = "Ronda de Capuchinos (Avenida Miraflores)",
            position = Stop.Position(latitude = 37.398807, longitude = -5.981822),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 24,
            description = "Ronda de Capuchinos (León XIII)",
            position = Stop.Position(latitude = 37.399669, longitude = -5.983595),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 25,
            description = "Muñoz León (Sánchez Perrier)",
            position = Stop.Position(latitude = 37.401397, longitude = -5.985486),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 238,
            description = "San Juan de Ribera (Macarena)",
            position = Stop.Position(latitude = 37.403549, longitude = -5.987237),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 239,
            description = "San Juan de Ribera (Policlínico)",
            position = Stop.Position(latitude = 37.405429, longitude = -5.986314),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 233,
            description = "Doctor Fedriani (Doctor Leal Castaños)",
            position = Stop.Position(latitude = 37.407817, longitude = -5.985156),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 251,
            description = "Doctor Fedriani (Plaza Isla Canela)",
            position = Stop.Position(latitude = 37.409427, longitude = -5.984575),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        ),
        Stop(
            code = 252,
            description = "Doctor Fedriani (Avda. San Lázaro)",
            position = Stop.Position(latitude = 37.411148, longitude = -5.983941),
            lines = lines.shuffled().take(Random.nextInt(1, 4)).map { it.toSummary()},
        )
    )

    val groupsOfLines = lines
        .groupBy { it.group }
        .map { (group, linesForGroup) -> LinesScreenState.Content.GroupOfLines(group, linesForGroup) }

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

    val routesWithStops: List<RouteWithStops> = routes.map {
        RouteWithStops(it, stops.shuffled())
    }

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

    val travelCardTransactions = listOf(
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[4], people = 2),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[4]),
        TravelCard.Transaction.TopUp(10000, LocalDateTime.now()),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[34], people = 2),
        TravelCard.Transaction.Trip(350, LocalDateTime.now(), lines[34]),
    )

    val searchResults: List<SearchResult>
        get() {
            val stops = stops.shuffled().take(20)
                .map { SearchResult.StopResult(it, lines.shuffled().take(Random.nextInt(1, 4))) }
            val lines = lines.shuffled().take(10)
                .map { SearchResult.LineResult(it) }
            return (stops + lines).shuffled()
        }

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

    suspend inline fun delayNetwork(): Unit = delay(1_000)

}

