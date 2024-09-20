package com.sloy.sevibus.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sloy.sevibus.ui.theme.SevColors
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

fun LineColor.primary(): Color = when (this) {
    LineColor.Red -> SevColors.Red
    LineColor.Orange -> SevColors.Yellow
    LineColor.Blue -> SevColors.Blue
    LineColor.Cyan -> SevColors.Cyan
    LineColor.Green -> SevColors.Green
    LineColor.Wine -> SevColors.Wine
    LineColor.Black -> SevColors.Purple
    LineColor.Unknown -> Color(0xff000000)
}

fun LineColor.soft(): Color = when (this) {
    LineColor.Red -> SevColors.RedSoft
    LineColor.Orange -> SevColors.YellowSoft
    LineColor.Blue -> SevColors.BlueSoft
    LineColor.Cyan -> SevColors.CyanSoft
    LineColor.Green -> SevColors.GreenSoft
    LineColor.Wine -> SevColors.WineSoft
    LineColor.Black -> SevColors.PurpleSoft
    LineColor.Unknown -> SevColors.Neutral500
}

fun LineColor.toArgb() = this.primary().toArgb()
