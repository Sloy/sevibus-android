package com.sloydev.sevibus.feature.lines

data class Line(
    val label: String,
    val description: String,
    val colorHex: Long,
) {
    val type: String
        get() = when (label) {
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