package com.sloydev.sevibus

import com.sloydev.sevibus.feature.cards.CardInfo
import com.sloydev.sevibus.feature.cards.CardTransaction
import com.sloydev.sevibus.feature.lines.Line
import com.sloydev.sevibus.feature.linestops.Stop
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

object Stubs {
    val lines = listOf(
        Line("01", "Plg. Norte H. Virgen del Rocio", 0xffF54129),
        Line("02", "Puerta Triana - Heliopolis", 0xffF54129),
        Line("03", "Bellavista-S. Jeronimo-Pino Montano", 0xffF7A800),
        Line("05", "Puerta Triana - Santa Aurelia", 0xffF54129),
        Line("06", "Gta. S. Lázaro - Hosp. V. Rocio", 0xffF54129),
        Line("10", "Ponce de León - San Jerónimo", 0xff000D6F),
        Line("11", "Ponce de León - Los Principes", 0xff000D6F),
        Line("12", "Ponce de León -  Pino Montano", 0xff000D6F),
        Line("13", "Plaza Duque Pino Montano", 0xff000D6F),
        Line("14", "Plaza Duque - Poligono Norte - Las Golondrinas", 0xff000D6F),
        Line("15", "Ponce de León - San Diego", 0xff000D6F),
        Line("16", "Rialto - Valdezorras", 0xff000D6F),
        Line("20", "Ponce de León - Polígono de San Pablo", 0xff000D6F),
        Line("21", "Plaza de Armas - Polígono San Pablo", 0xff000D6F),
        Line("22", "Prado San Sebastian - Sevilla Este", 0xff000D6F),
        Line("24", "Ponce de León - Juan XXIII - Palmete", 0xff000D6F),
        Line("25", "Prado - Rochelambert", 0xff000D6F),
        Line("26", "Prado - Cerro del Aguila", 0xff000D6F),
        Line("27", "Plaza Duque - Sevilla Este", 0xff000D6F),
        Line("28", "Prado - Alcosa", 0xff000D6F),
        Line("29", "Prado S. Sebastian - Torreblanca", 0xff000D6F),
        Line("30", "Prado San Sebastián - La Paz", 0xff000D6F),
        Line("31", "Prado San Sebastián - Polígono Sur", 0xff000D6F),
        Line("32", "Plz. Duque - Plg. Sur", 0xff000D6F),
        Line("34", "Prado San Sebastián - Los Bermejales", 0xff000D6F),
        Line("37", "Pta. Jerez - Pedro Salvador - Bellavista", 0xff000D6F),
        Line("38A", "Prado - Pitamo - Olavide", 0xff000D6F),
        Line("39", "Los Arcos - Hac. S. Antonio", 0xff000D6F),
        Line("40", "Reyes Catolicos - Triana", 0xff000D6F),
        Line("41", "Reyes Catolicos - Tablada", 0xff000D6F),
        Line("43", "San Pablo - El Tardon", 0xff000D6F),
        Line("52", "S. Bernardo - Palmete", 0xff000D6F),
        Line("B3", "Gran Plaza - Santa Clara", 0xff84C6E3),
        Line("B4", "San Bernardo - Torreblanca", 0xff84C6E3),
        Line("C1", "Circular Exterior 1", 0xff008431),
        Line("C2", "Circular Exterior 2", 0xff008431),
        Line("C3", "Circular Interior 1", 0xff008431),
        Line("C4", "Circular Interior 2", 0xff008431),
        Line("C6A", "Circular Macarena Norte Sentido A", 0xff008431),
        Line("C6B", "Circular Macarena Norte Sentido B", 0xff008431),
        Line("CJ", "San Bernardo - Ciudad de La Justicia", 0xffF7A800),
        Line("EA", "Aeropuerto", 0xff84C6E3),
        Line("EC", "Lanzadera Charco La Pava", 0xff84C6E3),
        Line("LE", "Prado San Sebastián - Sevilla Este", 0xffF7A800),
        Line("LN", "Line Norte", 0xffF7A800),
        Line("LS", "Santa Justa - Bellavista", 0xffF7A800),
        Line("T1", "Metrocentro", 0xffC60018)
    )

    val lineTypes = listOf(
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

    val stops = listOf(
        Stop(code = 430, description = "Glorieta Plus Ultra", position = Stop.Position(latitude = 37.356990, longitude = -5.980317)),
        Stop(
            code = 566,
            description = "Manuel Siurot (Rafael Salgado)",
            position = Stop.Position(latitude = 37.359176, longitude = -5.980631)
        ),
        Stop(
            code = 154,
            description = "Manuel Siurot (Hospital V.Rocio)",
            position = Stop.Position(latitude = 37.361072, longitude = -5.981739)
        ),
        Stop(
            code = 64,
            description = "Manuel Siurot (Gustavo Gallardo)",
            position = Stop.Position(latitude = 37.365230, longitude = -5.983750)
        ),
        Stop(
            code = 65,
            description = "Manuel Siurot (Edificio La Estrella)",
            position = Stop.Position(latitude = 37.367492, longitude = -5.985054)
        ),
        Stop(
            code = 66,
            description = "Avenida La Borbolla (Felipe Ii)",
            position = Stop.Position(latitude = 37.372348, longitude = -5.985392)
        ),
        Stop(
            code = 67,
            description = "Avenida La Borbolla (Montevideo)",
            position = Stop.Position(latitude = 37.374198, longitude = -5.985209)
        ),
        Stop(
            code = 68,
            description = "Avda. La Borbolla (Dr. Pedro de Castro)",
            position = Stop.Position(latitude = 37.379100, longitude = -5.983300)
        ),
        Stop(code = 472, description = "Menendez Pelayo (Juzgados)", position = Stop.Position(latitude = 37.381877, longitude = -5.987963)),
        Stop(
            code = 42,
            description = "Menéndez Pelayo (Puerta de La Carne)",
            position = Stop.Position(latitude = 37.386516, longitude = -5.985553)
        ),
        Stop(code = 43, description = "Recaredo (Puerta Carmona)", position = Stop.Position(latitude = 37.389663, longitude = -5.984265)),
        Stop(code = 888, description = "Recaredo (San Roque)", position = Stop.Position(latitude = 37.391544, longitude = -5.984123)),
        Stop(
            code = 22,
            description = "María Auxiliadora (Puerta Osario)",
            position = Stop.Position(latitude = 37.394493, longitude = -5.983595)
        ),
        Stop(
            code = 23,
            description = "Ronda de Capuchinos (Avenida Miraflores)",
            position = Stop.Position(latitude = 37.398807, longitude = -5.981822)
        ),
        Stop(
            code = 24,
            description = "Ronda de Capuchinos (León XIII)",
            position = Stop.Position(latitude = 37.399669, longitude = -5.983595)
        ),
        Stop(
            code = 25, description = "Muñoz León (Sánchez Perrier)", position = Stop.Position(latitude = 37.401397, longitude = -5.985486)
        ),
        Stop(
            code = 238, description = "San Juan de Ribera (Macarena)", position = Stop.Position(latitude = 37.403549, longitude = -5.987237)
        ),
        Stop(
            code = 239,
            description = "San Juan de Ribera (Policlínico)",
            position = Stop.Position(latitude = 37.405429, longitude = -5.986314)
        ),
        Stop(
            code = 233,
            description = "Doctor Fedriani (Doctor Leal Castaños)",
            position = Stop.Position(latitude = 37.407817, longitude = -5.985156)
        ),
        Stop(
            code = 251,
            description = "Doctor Fedriani (Plaza Isla Canela)",
            position = Stop.Position(latitude = 37.409427, longitude = -5.984575)
        ),
        Stop(
            code = 252,
            description = "Doctor Fedriani (Avda. San Lázaro)",
            position = Stop.Position(latitude = 37.411148, longitude = -5.983941)
        )
    )

    val cardWithAllFields = CardInfo(
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
        CardInfo(code = 31, title = "Saldo sin transbordo", customName = "Bono Laura", balanceMillis = 8450, serialNumber = randomCardSerial()),
        CardInfo(code = 70, title = "Hijo de empleado", customName = "La de paco", balanceTrips = 35, serialNumber = randomCardSerial()),
        CardInfo(code = 116, title = "Estudiante mensual", customName = "Estudiante", validityEnd = LocalDate.now(), extensionEnd = LocalDate.now(), serialNumber = randomCardSerial()),
        CardInfo(code = 30, title = "Saldo con transbordo", customName = "Números rojos", balanceMillis = -450, serialNumber = randomCardSerial()),
    )

    private fun randomCardSerial() = Random.nextLong(100000000, 999999999)

    val cardTransactions = listOf(
        CardTransaction.Validation(350, LocalDateTime.now(), lines[4], people = 2),
        CardTransaction.Validation(350, LocalDateTime.now(), lines[4]),
        CardTransaction.TopUp(10000, LocalDateTime.now()),
        CardTransaction.Validation(350, LocalDateTime.now(), lines[34], people = 2),
        CardTransaction.Validation(350, LocalDateTime.now(), lines[34]),
    )
}

