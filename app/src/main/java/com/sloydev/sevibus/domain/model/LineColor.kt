package com.sloydev.sevibus.domain.model

import androidx.compose.ui.graphics.Color
import com.sloydev.sevibus.ui.theme.AlexGreen
import com.sloydev.sevibus.ui.theme.AlexOrange
import com.sloydev.sevibus.ui.theme.AlexPink
import com.sloydev.sevibus.ui.theme.AlexPurple
import kotlinx.serialization.Serializable

@Serializable
enum class LineColor {
    Red,
    Orange,
    Blue,
    Cyan,
    Green,
    Wine,
    Black,
    Unknown,
}

fun LineColor.toUiColor(): Color = when (this) {
    LineColor.Red -> AlexPink// Color(0xfff54129)
    LineColor.Orange -> AlexOrange// Color(0xfff7a800)
    LineColor.Blue -> AlexPurple//Color(0xff000d6f)
    LineColor.Cyan -> Color(0xff84c6e3)
    LineColor.Green -> AlexGreen// Color(0xff008431)
    LineColor.Wine -> Color(0xffc60018)
    LineColor.Black -> Color(0xff000000)
    LineColor.Unknown -> Color(0xff000000)
}