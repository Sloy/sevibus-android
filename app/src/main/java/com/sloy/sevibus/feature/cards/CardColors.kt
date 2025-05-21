package com.sloy.sevibus.feature.cards

import androidx.compose.ui.graphics.Color
import com.sloy.sevibus.domain.model.CardInfo

object CardColors {

    fun getCardColors(card: CardInfo): Pair<Color, Color> {
        return cardColors.getValue(card.code)
    }

    private val cardColors = mapOf(
        30 to (Color(0xFFB6003B) to Color.White), // Saldo con transbordo
        31 to (Color.White to Color(0xFFB6003B)), // Saldo sin transbordo
        70 to (Color(0xFF41B48D) to Color.White), // Hijo de empleado
        71 to (Color(0xFFCE757C) to Color.White), // Familiar de empleado
        101 to (Color(0xFFDE023F) to Color(0xFF7FBA5F)), // Joven
        102 to (Color(0xFFFBF9DE) to Color(0xFFF9BC00)), // ???
        103 to (Color(0xFF816CAB) to Color(0xFFCDC7EE)), // Solidaria
        104 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad //TODO el texto tiene color diferente!
        105 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida?)
        109 to (Color(0xFFD47589) to Color(0xFFF5ECD1)), // Social
        110 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida x2?)
        111 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida x3)
        112 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida x3)
        113 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida x3)
        114 to (Color(0xFF99D5E8) to Color(0xFFF4D6D8)), // 3ª edad (repetida x3)
        115 to (Color(0xFF95B0BB) to Color(0xFFF53146)), // Estudiante
        116 to (Color(0xFFFEF1C6) to Color(0xFF5AB2A4)), // Estudiante mensual
        117 to (Color(0xFF157F87) to Color(0xFF97C8A9)), // Diversidad funcional
        118 to (Color(0xFFAED29C) to Color(0xFFE35B78)), // Infantil
        150 to (Color(0xFFFEE4A1) to Color(0xFFE85F3E)), // 30 días
        151 to (Color(0xFFEBCFC4) to Color(0xFF006092)), // Turística 1 día
        152 to (Color(0xFFEBD3C8) to Color(0xFF81184F)), // Turística 3 días
        154 to (Color(0xFF0167AE) to Color(0xFFBFDDF2)), // Familia numerosa
        155 to (Color(0xFF00ACC8) to Color(0xFFDCE9F6)), // Familia numerosa especial
        156 to (Color(0xFFC1A172) to Color(0xFF078371)), // Anual
        157 to (Color(0xFFFFF4AD) to Color(0xFFA2A8B1)), // Mensual aeropuerto
        201 to (Color(0xFF013964) to Color.White), // Pensionista
    ).withDefault { Color.Gray to Color.White }
}
