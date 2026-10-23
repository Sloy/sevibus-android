package com.sloy.sevibus

import com.sloy.sevibus.data.api.model.HealthCheckDto
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteWithStops
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.feature.foryou.nearby.NearbyStop
import com.sloy.sevibus.feature.lines.LinesScreenState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Deprecated("")
object Stubs {

    // Fixed instead of the default LocalTime.now(), so previews render the same at any time of day
    private val allDaySchedule = Route.Schedule(LocalTime.MIN, LocalTime.MAX)

    @Deprecated("")
    val routes: List<Route> = listOf(
        Route(
            id = "1.1",
            direction = 1,
            destination = "Hospital V. Rocío",
            line = 1,
            stops = listOf(1, 2, 3, 4, 5),
            schedule = allDaySchedule,
        ),
        Route(
            id = "1.2",
            direction = 2,
            destination = "Polígono Norte",
            line = 1,
            stops = listOf(10, 9, 8, 7, 6),
            schedule = allDaySchedule,
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
        Line("C1", "Circular Exterior 1", LineColor.Green, routes = routes.take(1), id = 1001),
        Line("C2", "Circular Exterior 2", LineColor.Green, routes = routes.take(1), id = 1002),
        Line("C3", "Circular Interior 1", LineColor.Green, routes = routes.take(1), id = 1003),
        Line("C4", "Circular Interior 2", LineColor.Green, routes = routes.take(1), id = 1004),
        Line("C6A", "Circular Macarena Norte Sentido A", LineColor.Green, routes = routes.take(1), id = 1006),
        Line("C6B", "Circular Macarena Norte Sentido B", LineColor.Green, routes = routes.take(1), id = 1007),
        Line("CJ", "San Bernardo - Ciudad de La Justicia", LineColor.Orange, routes = routes.take(1), id = 100),
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
            lines = listOf(lines[0], lines[1], lines[2]).map { it.toSummary() },
        ),
        Stop(
            code = 566,
            description = "Manuel Siurot (Rafael Salgado)",
            position = Position(latitude = 37.359176, longitude = -5.980631),
            lines = listOf(lines[3], lines[4]).map { it.toSummary() },
        ),
        Stop(
            code = 154,
            description = "Manuel Siurot (Hospital V.Rocio)",
            position = Position(latitude = 37.361072, longitude = -5.981739),
            lines = listOf(lines[5]).map { it.toSummary() },
        ),
        Stop(
            code = 64,
            description = "Manuel Siurot (Gustavo Gallardo)",
            position = Position(latitude = 37.365230, longitude = -5.983750),
            lines = listOf(lines[6], lines[7], lines[8], lines[9]).map { it.toSummary() },
        ),
        Stop(
            code = 65,
            description = "Manuel Siurot (Edificio La Estrella)",
            position = Position(latitude = 37.367492, longitude = -5.985054),
            lines = listOf(lines[10], lines[11]).map { it.toSummary() },
        ),
        Stop(
            code = 66,
            description = "Avenida La Borbolla (Felipe Ii)",
            position = Position(latitude = 37.372348, longitude = -5.985392),
            lines = listOf(lines[12], lines[13], lines[14]).map { it.toSummary() },
        ),
        Stop(
            code = 67,
            description = "Avenida La Borbolla (Montevideo)",
            position = Position(latitude = 37.374198, longitude = -5.985209),
            lines = listOf(lines[15]).map { it.toSummary() },
        ),
        Stop(
            code = 68,
            description = "Avda. La Borbolla (Dr. Pedro de Castro)",
            position = Position(latitude = 37.379100, longitude = -5.983300),
            lines = listOf(lines[16], lines[17]).map { it.toSummary() },
        ),
        Stop(
            code = 472,
            description = "Menendez Pelayo (Juzgados)",
            position = Position(latitude = 37.381877, longitude = -5.987963),
            lines = listOf(lines[18], lines[19], lines[20]).map { it.toSummary() },
        ),
        Stop(
            code = 42,
            description = "Menéndez Pelayo (Puerta de La Carne)",
            position = Position(latitude = 37.386516, longitude = -5.985553),
            lines = listOf(lines[21]).map { it.toSummary() },
        ),
        Stop(
            code = 43,
            description = "Recaredo (Puerta Carmona)",
            position = Position(latitude = 37.389663, longitude = -5.984265),
            lines = listOf(lines[22], lines[23]).map { it.toSummary() },
        ),
        Stop(
            code = 888,
            description = "Recaredo (San Roque)",
            position = Position(latitude = 37.391544, longitude = -5.984123),
            lines = listOf(lines[24], lines[25], lines[26]).map { it.toSummary() },
        ),
        Stop(
            code = 22,
            description = "María Auxiliadora (Puerta Osario)",
            position = Position(latitude = 37.394493, longitude = -5.983595),
            lines = listOf(lines[27]).map { it.toSummary() },
        ),
        Stop(
            code = 23,
            description = "Ronda de Capuchinos (Avenida Miraflores)",
            position = Position(latitude = 37.398807, longitude = -5.981822),
            lines = listOf(lines[28], lines[29]).map { it.toSummary() },
        ),
        Stop(
            code = 24,
            description = "Ronda de Capuchinos (León XIII)",
            position = Position(latitude = 37.399669, longitude = -5.983595),
            lines = listOf(lines[30], lines[31], lines[32]).map { it.toSummary() },
        ),
        Stop(
            code = 25,
            description = "Muñoz León (Sánchez Perrier)",
            position = Position(latitude = 37.401397, longitude = -5.985486),
            lines = listOf(lines[33]).map { it.toSummary() },
        ),
        Stop(
            code = 238,
            description = "San Juan de Ribera (Macarena)",
            position = Position(latitude = 37.403549, longitude = -5.987237),
            lines = listOf(lines[34], lines[35]).map { it.toSummary() },
        ),
        Stop(
            code = 239,
            description = "San Juan de Ribera (Policlínico)",
            position = Position(latitude = 37.405429, longitude = -5.986314),
            lines = listOf(lines[36], lines[37], lines[38]).map { it.toSummary() },
        ),
        Stop(
            code = 233,
            description = "Doctor Fedriani (Doctor Leal Castaños)",
            position = Position(latitude = 37.407817, longitude = -5.985156),
            lines = listOf(lines[39]).map { it.toSummary() },
        ),
        Stop(
            code = 251,
            description = "Doctor Fedriani (Plaza Isla Canela)",
            position = Position(latitude = 37.409427, longitude = -5.984575),
            lines = listOf(lines[40], lines[41]).map { it.toSummary() },
        ),
        Stop(
            code = 252,
            description = "Doctor Fedriani (Avda. San Lázaro)",
            position = Position(latitude = 37.411148, longitude = -5.983941),
            lines = listOf(lines[42], lines[43]).map { it.toSummary() },
        )
    )

    @Deprecated("")
    val favorites = listOf(
        FavoriteStop(
            customName = "Casa",
            customIcon = CustomIcon.Home,
            stop = stops[1],
            selectedLineIds = stops[1].lines.take(2).map { it.id }.toSet()
        ),
        FavoriteStop(
            customName = "Oficina",
            customIcon = CustomIcon.Office,
            stop = stops[0],
            selectedLineIds = stops[0].lines.takeLast(1).map { it.id }.toSet()
        ),
        FavoriteStop(
            customName = null,
            customIcon = null,
            stop = stops[2],
            selectedLineIds = null
        ),
        FavoriteStop(
            customName = null,
            customIcon = null,
            stop = stops[3],
            selectedLineIds = stops[3].lines.drop(1).take(2).map { it.id }.toSet()
        ),
    )

    @Deprecated("")
    val nearby = listOf(
        NearbyStop(stops[1], distance = 25),
        NearbyStop(stops[0], distance = 42),
        NearbyStop(stops[2], distance = 270),
        NearbyStop(stops[3], distance = 192),
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
    val routesWithStops: List<RouteWithStops> = routes.mapIndexed { index, route ->
        val stopsForRoute = if (index % 2 == 0) {
            stops.take(10)
        } else {
            stops.drop(5).take(10)
        }
        RouteWithStops(route, stopsForRoute)
    }

    @Deprecated("")
    val cardWithAllFields = CardInfo.BalanceCard(
        code = 30,
        type = "Full test",
        customName = "La de Laura",
        balance = Money(8450),
        serialNumber = 794836666,
    )

    @Deprecated("")
    val cards: List<CardInfo> = listOf(
        CardInfo.BalanceCard(
            code = 31,
            type = "Sin transbordo",
            customName = "Bono Laura",
            balance = Money(845),
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 70,
            type = "Hijo de empleado",
            validity = null,
            customName = "La de paco",
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 116,
            type = "Estudiante mensual",
            validity = null,
            customName = "Estudiante",
            serialNumber = nextCardSerial()
        ),
        CardInfo.BalanceCard(
            code = 30,
            type = "Saldo con transbordo",
            customName = "Números rojos",
            balance = Money(-45),
            serialNumber = nextCardSerial()
        ),
        CardInfo.BalanceCard(
            code = 117,
            type = "Diversidad funcional",
            customName = null,
            balance = Money(-45),
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 71,
            type = "Familiar de empleado",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 101,
            type = "Joven",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 102,
            type = "Algo de Tussam",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 103,
            type = "Solidaria",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 104,
            type = "3ª edad",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 109,
            type = "Social",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 115,
            type = "Estudiante",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 116,
            type = "Estudiante mensual",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 117,
            type = "Diversidad funcional",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 118,
            type = "Infantil",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 150,
            type = "30 días",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 151,
            type = "Turística 1 día",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 152,
            type = "Turística 3 días",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 154,
            type = "Familia numerosa",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 155,
            type = "Familia numerosa especial",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 156,
            type = "Anual",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
        CardInfo.PassCard(
            code = 201,
            type = "Pensionista",
            validity = null,
            customName = null,
            serialNumber = nextCardSerial()
        ),
    )

    val balanceCard: CardInfo.BalanceCard
        get() = cards[0] as CardInfo.BalanceCard

    val negativeBalanceCard: CardInfo.BalanceCard
        get() = cards[3] as CardInfo.BalanceCard

    private var cardSerialCounter = 100000000L
    private fun nextCardSerial(): CardId = cardSerialCounter++

    // Fixed date so screenshot tests are deterministic
    private val transactionsDate = LocalDateTime.of(2025, 12, 31, 14, 46)

    @Deprecated("")
    val cardInfoTransactions: List<CardTransaction> = listOf(
        cardTrip(700000001, transactionsDate, lines[4].label, fare = 700, balanceAfter = 9300, people = 2),
        cardTrip(700000002, transactionsDate, lines[2].label, fare = 350, balanceAfter = 10000),
        CardTransaction.TopUp(700000003, transactionsDate, Money(10000), Money(10350)),
        cardTrip(700000004, transactionsDate, lines[34].label, fare = 700, balanceAfter = 350, people = 2),
        cardTrip(700000005, transactionsDate, lines[34].label, fare = 350, balanceAfter = 1050),
    )

    @Deprecated("")
    val cardTripTransactions: List<CardTransaction> = listOf(
        cardTrip(700000011, transactionsDate, lines[4].label, fare = 41, balanceAfter = 804),
        cardTrip(700000012, transactionsDate.minusDays(1), lines[2].label, fare = 41, balanceAfter = 845),
    )

    data class CardCase(val name: String, val card: CardInfo, val transactions: List<CardTransaction>)

    val cardCasesToday: LocalDate = LocalDate.of(2026, 10, 10)

    private fun cardLine(label: String): LineSummary =
        lines.find { it.label == label }?.toSummary() ?: LineSummary(-1, label, LineColor.Unknown)

    private fun cardTrip(
        serialNumber: CardId,
        date: LocalDateTime,
        line: String,
        fare: Int? = null,
        balanceAfter: Int? = null,
        people: Int = 1,
        transfers: List<CardTransaction.Transfer> = emptyList(),
    ) = CardTransaction.Trip(serialNumber, date, cardLine(line), people, balanceAfter?.let { Fare(Money(fare ?: 0), Money(it)) }, transfers)

    private fun cardTransfer(serialNumber: CardId, date: LocalDateTime, line: String) =
        CardTransaction.Transfer(serialNumber, date, cardLine(line))

    private fun passValidity(from: LocalDate, to: LocalDate, nextPeriod: NextPeriod? = null) =
        PassValidity(PassPeriod(from, to), nextPeriod)

    val cardCases: List<CardCase> = listOf(
        CardCase(
            name = "BalanceNormal",
            card = CardInfo.BalanceCard(900000101, 31, "Bonobús sin transbordo", Money(869)),
            transactions = listOf(
                cardTrip(900000101, LocalDateTime.of(2026, 10, 5, 17, 6, 28), "21", fare = 41, balanceAfter = 869),
                cardTrip(900000101, LocalDateTime.of(2026, 10, 1, 10, 28, 59), "41", fare = 41, balanceAfter = 910),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 30, 20, 24, 7), "41", fare = 41, balanceAfter = 951),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 29, 10, 11, 5), "41", fare = 41, balanceAfter = 992),
                CardTransaction.TopUp(900000101, LocalDateTime.of(2026, 9, 28, 12, 46, 3), Money(1000), Money(1074)),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 22, 10, 10, 18), "41", balanceAfter = 74),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 21, 13, 18, 40), "41", fare = 41, balanceAfter = 74),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 19, 21, 11, 36), "41", fare = 41, balanceAfter = 115),
                cardTrip(900000101, LocalDateTime.of(2026, 9, 19, 20, 5, 12), "41", fare = 41, balanceAfter = 156),
            ),
        ),
        CardCase(
            name = "BalanceGroup",
            card = CardInfo.BalanceCard(900000102, 30, "Bonobús con transbordo", Money(452)),
            transactions = listOf(
                cardTrip(900000102, LocalDateTime.of(2025, 10, 28, 11, 35, 3), "15", fare = 46, balanceAfter = 452),
                cardTrip(900000102, LocalDateTime.of(2025, 8, 15, 16, 24, 39), "12", fare = 92, balanceAfter = 498, people = 2),
                cardTrip(900000102, LocalDateTime.of(2025, 8, 15, 14, 14, 40), "12", fare = 92, balanceAfter = 590, people = 2),
                cardTrip(
                    900000102, LocalDateTime.of(2025, 7, 9, 20, 5, 26), "12", fare = 92, balanceAfter = 682, people = 2,
                    transfers = listOf(cardTransfer(900000102, LocalDateTime.of(2025, 7, 9, 20, 17, 30), "02")),
                ),
                CardTransaction.TopUp(900000102, LocalDateTime.of(2025, 7, 9, 20, 4, 26), Money(700), Money(774)),
            ),
        ),
        CardCase(
            name = "BalanceLow",
            card = CardInfo.BalanceCard(900000103, 30, "Bonobús con transbordo", Money(10)),
            transactions = listOf(
                cardTrip(900000103, LocalDateTime.of(2026, 5, 16, 13, 57, 16), "13", fare = 46, balanceAfter = 10),
                cardTrip(900000103, LocalDateTime.of(2026, 5, 16, 13, 18, 18), "13", fare = 46, balanceAfter = 56),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 28, 15, 57, 14), "27", fare = 46, balanceAfter = 102),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 27, 16, 17, 25), "03", fare = 46, balanceAfter = 148),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 27, 15, 47, 10), "03", fare = 46, balanceAfter = 194),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 26, 16, 10, 23), "27", fare = 46, balanceAfter = 240),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 25, 20, 9, 53), "03", fare = 46, balanceAfter = 286),
                cardTrip(900000103, LocalDateTime.of(2026, 3, 25, 17, 26, 29), "03", fare = 46, balanceAfter = 332),
                cardTrip(900000103, LocalDateTime.of(2026, 2, 20, 8, 46, 36), "02", fare = 46, balanceAfter = 378),
                cardTrip(900000103, LocalDateTime.of(2026, 2, 19, 8, 50, 11), "02", fare = 46, balanceAfter = 424),
            ),
        ),
        CardCase(
            name = "BalanceNegative",
            card = CardInfo.BalanceCard(900000104, 30, "Bonobús con transbordo", Money(-44)),
            transactions = listOf(
                CardTransaction.PendingPurchase(900000104, LocalDateTime.of(2026, 10, 9, 21, 40, 0), Money(1000)),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 8, 1, 13, 54), "29", fare = 46, balanceAfter = -44),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 7, 16, 11, 3), "29", fare = 46, balanceAfter = 2),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 6, 16, 37, 36), "02", fare = 46, balanceAfter = 48),
                cardTrip(
                    900000104, LocalDateTime.of(2026, 10, 6, 13, 32, 50), "29", fare = 46, balanceAfter = 94,
                    transfers = listOf(cardTransfer(900000104, LocalDateTime.of(2026, 10, 6, 13, 58, 6), "02")),
                ),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 4, 18, 26, 44), "29", fare = 46, balanceAfter = 140),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 4, 9, 30, 8), "29", fare = 46, balanceAfter = 186),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 3, 23, 15, 6), "29", fare = 46, balanceAfter = 232),
                cardTrip(900000104, LocalDateTime.of(2026, 10, 3, 14, 27, 13), "29", fare = 46, balanceAfter = 278),
            ),
        ),
        CardCase(
            name = "PassActive",
            card = CardInfo.PassCard(
                900000105, 150, "Tarjeta 30 días",
                passValidity(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4)),
                expiresOn = LocalDate.of(2040, 8, 12),
            ),
            transactions = listOf(
                cardTrip(900000105, LocalDateTime.of(2026, 10, 9, 19, 45, 25), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 9, 7, 38, 8), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 8, 19, 58, 47), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 8, 7, 51, 8), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 7, 19, 52, 48), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 7, 7, 45, 18), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 6, 19, 57, 26), "02"),
                cardTrip(900000105, LocalDateTime.of(2026, 10, 6, 7, 35, 46), "02"),
                CardTransaction.PassPurchase(900000105, LocalDateTime.of(2026, 10, 5, 7, 48, 42), Money(2120)),
            ),
        ),
        CardCase(
            name = "PassNextPeriod",
            card = CardInfo.PassCard(
                900000106, 154, "Familia numerosa",
                passValidity(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod.OnFirstUse),
                expiresOn = LocalDate.of(2028, 5, 21),
            ),
            transactions = listOf(
                cardTrip(900000106, LocalDateTime.of(2026, 10, 9, 20, 16, 5), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 9, 15, 43, 20), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 9, 13, 41, 32), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 9, 8, 45, 42), "02"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 9, 7, 35, 53), "12"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 8, 20, 14, 45), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 8, 15, 39, 33), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 8, 13, 40, 36), "32"),
                cardTrip(900000106, LocalDateTime.of(2026, 10, 8, 8, 41, 32), "02"),
                CardTransaction.PassRenewal(900000106, LocalDateTime.of(2026, 10, 8, 7, 38, 3), Money(1690)),
            ),
        ),
        CardCase(
            name = "PassExpired",
            card = CardInfo.PassCard(900000107, 103, "Bonobús solidario", passValidity(LocalDate.of(2026, 1, 28), LocalDate.of(2026, 7, 26))),
            transactions = listOf(
                cardTrip(900000107, LocalDateTime.of(2026, 7, 18, 23, 27, 31), "T1"),
                cardTrip(900000107, LocalDateTime.of(2026, 7, 18, 21, 13, 25), "T1"),
                cardTrip(900000107, LocalDateTime.of(2026, 6, 16, 9, 55, 27), "22"),
                cardTrip(900000107, LocalDateTime.of(2026, 6, 15, 13, 3, 15), "06"),
                cardTrip(
                    900000107, LocalDateTime.of(2026, 6, 15, 11, 9, 30), "05",
                    transfers = listOf(cardTransfer(900000107, LocalDateTime.of(2026, 6, 15, 11, 37, 50), "C1")),
                ),
                cardTrip(
                    900000107, LocalDateTime.of(2026, 6, 11, 12, 43, 46), "02",
                    transfers = listOf(cardTransfer(900000107, LocalDateTime.of(2026, 6, 11, 13, 2, 51), "03")),
                ),
                cardTrip(900000107, LocalDateTime.of(2026, 6, 8, 9, 45, 9), "52"),
                cardTrip(900000107, LocalDateTime.of(2026, 6, 1, 10, 44, 39), "05"),
            ),
        ),
        CardCase(
            name = "PassTransfers",
            card = CardInfo.PassCard(900000109, 103, "Bonobús solidario", passValidity(LocalDate.of(2026, 8, 17), LocalDate.of(2027, 2, 16))),
            transactions = listOf(
                cardTrip(
                    900000109, LocalDateTime.of(2026, 10, 6, 11, 46, 12), "29",
                    transfers = listOf(cardTransfer(900000109, LocalDateTime.of(2026, 10, 6, 12, 24, 32), "02")),
                ),
                cardTrip(
                    900000109, LocalDateTime.of(2026, 10, 5, 12, 56, 17), "29",
                    transfers = listOf(
                        cardTransfer(900000109, LocalDateTime.of(2026, 10, 5, 13, 18, 19), "B4"),
                        cardTransfer(900000109, LocalDateTime.of(2026, 10, 5, 13, 45, 39), "28"),
                    ),
                ),
                cardTransfer(900000109, LocalDateTime.of(2026, 10, 4, 15, 13, 49), "01"),
            ),
        ),
        CardCase(
            name = "CardExpired",
            card = CardInfo.ExpiredCard(900000108, 116, "Estudiante mensual", expiresOn = LocalDate.of(2026, 8, 1)),
            transactions = listOf(
                cardTrip(900000108, LocalDateTime.of(2026, 4, 8, 12, 57, 48), "02"),
                cardTrip(
                    900000108, LocalDateTime.of(2026, 4, 8, 10, 29, 6), "37",
                    transfers = listOf(cardTransfer(900000108, LocalDateTime.of(2026, 4, 8, 10, 47, 30), "C4")),
                ),
                cardTrip(900000108, LocalDateTime.of(2026, 4, 7, 19, 29, 6), "02"),
                CardTransaction.PassPurchase(900000108, LocalDateTime.of(2026, 4, 6, 11, 2, 32), Money(1140)),
                cardTrip(
                    900000108, LocalDateTime.of(2026, 3, 23, 19, 57, 39), "C3",
                    transfers = listOf(cardTransfer(900000108, LocalDateTime.of(2026, 3, 23, 20, 17, 27), "37")),
                ),
                cardTrip(900000108, LocalDateTime.of(2026, 3, 23, 17, 1, 30), "02"),
                cardTrip(900000108, LocalDateTime.of(2026, 3, 17, 19, 46, 3), "01"),
            ),
        ),
    )

    @Deprecated("")
    val searchResults: List<SearchResult>
        get() {
            val stops = listOf(stops[0], stops[2], stops[5], stops[8], stops[11])
                .map { SearchResult.StopResult(it) }
            val lines = listOf(lines[1], lines[7], lines[15])
                .map { SearchResult.LineResult(it) }
            return (stops + lines)
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
        BusArrival.Available.Arriving(
            bus = 1,
            distance = 50,
            line = lines[0].toSummary(),
            route = routes[0],
            isLastBus = false
        ),
        BusArrival.Available.Estimation(
            bus = 6,
            distance = 50,
            seconds = 2 * 60,
            line = lines[1].toSummary(),
            route = routes[1],
            isLastBus = false
        ),
        BusArrival.Available.Estimation(
            bus = 2,
            distance = 50,
            seconds = 5 * 60,
            line = lines[2].toSummary(),
            route = routes[0],
            isLastBus = false
        ),
        BusArrival.Available.Estimation(
            bus = 3,
            distance = 100,
            seconds = 10 * 60,
            line = lines[2].toSummary(),
            route = routes[1],
            isLastBus = false
        ),
        BusArrival.Available.NoEstimation(
            bus = 4,
            distance = 200,
            line = lines[4].toSummary(),
            route = routes[0],
            isLastBus = false
        ),
        BusArrival.NotAvailable(line = lines[5].toSummary(), route = routes[1])
    )

    suspend inline fun delayNetwork(): Unit = delay(1_000)

    @Deprecated("Be careful using Stubs")
    val locationTriana = Position(37.385222, -6.011210)

    @Deprecated("Be careful using Stubs")
    val locationRecaredo = Position(37.389083, -5.984483)

    val locationSouth = Position(37.34274628939858, -5.982401669025421)

    val userLaura = LoggedUser(
        id = "x",
        displayName = "Laura Suárez",
        email = "laurasuarezlagares@gmail.com",
        photoUrl = "https://lh3.googleusercontent.com/-YUwtaYpq3TE/AAAAAAAAAAI/AAAAAAAAABc/n77IYIR9Zho/s96-c/photo.jpg",
    )
    val userNoPhoto = LoggedUser(
        id = "x",
        displayName = "Bonifacio Ramírez Alcántara",
        email = "boni@gmail.com",
        photoUrl = null,
    )

    val healthCheck = HealthCheckDto(
        timestamp = "2025-06-22T17:17:39.957Z",
        uptime = 6.649698565,
        environment = "development",
        database = "none",
        provider = "preview",
        version = "25.06.22-7e103da*",
        clientVersion = "0.0.0",
        host = "localhost:8080",
        ip = "185.251.208.19"
    )
}

