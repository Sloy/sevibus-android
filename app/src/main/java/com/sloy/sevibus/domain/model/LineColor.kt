package com.sloy.sevibus.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import com.sloy.sevibus.ui.theme.SevAlpha
import com.sloy.sevibus.ui.theme.SevColors
import com.sloy.sevibus.ui.theme.SevTheme
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

@Composable
fun LineColor.soft(): Color {
    return this.primary().copy(alpha = SevAlpha.LineColorSoft).compositeOver(SevTheme.colorScheme.background)
}

fun LineColor.toArgb() = this.primary().toArgb()
