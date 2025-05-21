package com.sloy.sevibus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.domain.model.soft

private val DarkColorScheme = darkColorScheme(
    primary = SevColors.Red,
    onPrimary = SevColors.Neutral900_Dark,
    primaryContainer = SevColors.Red.copy(alpha = SevAlpha.Medium_Dark),
    onPrimaryContainer = SevColors.Neutral500_Dark,

    secondary = SevColors.Pink,
    onSecondary = SevColors.Neutral0_Dark,
    secondaryContainer = SevColors.Pink.copy(alpha = SevAlpha.Medium_Dark),
    onSecondaryContainer = SevColors.Neutral500_Dark,

    tertiary = SevColors.Lavender,
    onTertiary = SevColors.Neutral0_Dark,
    tertiaryContainer = SevColors.Lavender.copy(alpha = SevAlpha.Medium_Dark),
    onTertiaryContainer = SevColors.Neutral500_Dark,

    background = SevColors.Neutral0_Dark,
    onBackground = SevColors.Neutral900_Dark,

    surface = SevColors.Neutral50_Dark,
    onSurface = SevColors.Neutral900_Dark,
    surfaceVariant = SevColors.Neutral500_Dark.copy(alpha = SevAlpha.Medium_Dark),
    onSurfaceVariant = SevColors.Neutral500_Dark,
    surfaceTint = SevColors.Neutral50_Dark, // Applied to override elevation tint in Surfaces (see https://stackoverflow.com/a/76160786)

    inversePrimary = SevColors.Red,
    outline = SevColors.OpacityMedium,
    outlineVariant = SevColors.OpacitySoft,
    surfaceContainerLowest = SevColors.Neutral0_Dark,
    surfaceContainerLow = SevColors.Neutral50_Dark,
    surfaceContainer = SevColors.Neutral0_Dark,
    surfaceContainerHigh = SevColors.Neutral50_Dark,
    surfaceContainerHighest = SevColors.Neutral50_Dark,
)

private val LightColorScheme = lightColorScheme(
    primary = SevColors.Red,
    onPrimary = SevColors.Neutral0,
    primaryContainer = SevColors.Red.copy(alpha = SevAlpha.Medium),
    onPrimaryContainer = SevColors.Neutral500,

    secondary = SevColors.Pink,
    onSecondary = SevColors.Neutral0,
    secondaryContainer = SevColors.Pink.copy(alpha = SevAlpha.Medium),
    onSecondaryContainer = SevColors.Neutral500,

    tertiary = SevColors.Lavender,
    onTertiary = SevColors.Neutral0,
    tertiaryContainer = SevColors.Lavender.copy(alpha = SevAlpha.Medium),
    onTertiaryContainer = SevColors.Neutral500,

    background = SevColors.Neutral0,
    onBackground = SevColors.Neutral900,

    surface = SevColors.Neutral50,
    onSurface = SevColors.Neutral900,
    surfaceVariant = SevColors.Neutral500.copy(alpha = SevAlpha.Medium),
    onSurfaceVariant = SevColors.Neutral500,
    surfaceTint = SevColors.Neutral50, // Applied to override elevation tint in Surfaces (see https://stackoverflow.com/a/76160786)

//    inverseSurface = ,
//    inverseOnSurface = ,
    inversePrimary = SevColors.Red,

    outline = SevColors.OpacityMedium,
    outlineVariant = SevColors.OpacitySoft,

    surfaceContainerLowest = SevColors.Neutral0,
    surfaceContainerLow = SevColors.Neutral0,
    surfaceContainer = SevColors.Neutral50,
    surfaceContainerHigh = SevColors.Neutral50,
    surfaceContainerHighest = SevColors.Neutral50,
)

@Composable
fun SevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography(
            // Used for the LargeTopAppBar
            titleLarge = SevTheme.typography.headingSmall,
            headlineMedium = SevTheme.typography.headingLarge,
        )
    )
}

object SevTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: SevTypography = SevTypography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    @Composable
    fun WithLineColors(lineColor: LineColor, content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = colorScheme.copy(primary = lineColor.primary(), primaryContainer = lineColor.soft())
        ) {
            content()
        }
    }
}

