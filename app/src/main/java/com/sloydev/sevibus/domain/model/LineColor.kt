package com.sloydev.sevibus.domain.model

import androidx.compose.ui.graphics.Color
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
    LineColor.Red -> Color(0xfff54129)
    LineColor.Orange -> Color(0xfff7a800)
    LineColor.Blue -> Color(0xff000d6f)
    LineColor.Cyan -> Color(0xff84c6e3)
    LineColor.Green -> Color(0xff008431)
    LineColor.Wine -> Color(0xffc60018)
    LineColor.Black -> Color(0xff000000)
    LineColor.Unknown -> Color(0xff000000)
}