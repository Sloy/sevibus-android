package com.sloy.debugmenu.events

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal object EventText {
    val Mono12 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp)
    val Mono12Medium = Mono12.copy(fontWeight = FontWeight.Medium)
    val Mono11 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
    val Mono11Medium = Mono11.copy(fontWeight = FontWeight.Medium)
    val Mono10 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
    val Mono10Medium = Mono10.copy(fontWeight = FontWeight.Medium)
    val Mono9 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 12.sp)
    val Mono9Medium = Mono9.copy(fontWeight = FontWeight.Medium)
}
