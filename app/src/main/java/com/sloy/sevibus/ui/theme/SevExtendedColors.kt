package com.sloy.sevibus.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SevExtendedColors(
    val warningSurface: Color,
    val onWarningSurface: Color,
    val onWarningSurfaceVariant: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val tussamRed: Color,
)

internal val LightExtendedColors = SevExtendedColors(
    warningSurface = SevColors.WarningSurface,
    onWarningSurface = SevColors.OnWarningSurface,
    onWarningSurfaceVariant = SevColors.OnWarningSurfaceVariant,
    warningContainer = SevColors.WarningContainer,
    onWarningContainer = SevColors.OnWarningContainer,
    tussamRed = SevColors.TussamRed,
)

internal val DarkExtendedColors = SevExtendedColors(
    warningSurface = SevColors.WarningSurface_Dark,
    onWarningSurface = SevColors.OnWarningSurface_Dark,
    onWarningSurfaceVariant = SevColors.OnWarningSurfaceVariant_Dark,
    warningContainer = SevColors.WarningContainer_Dark,
    onWarningContainer = SevColors.OnWarningContainer_Dark,
    tussamRed = SevColors.TussamRed,
)

internal val LocalSevExtendedColors = staticCompositionLocalOf { LightExtendedColors }
