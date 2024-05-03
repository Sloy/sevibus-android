package com.sloydev.sevibus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sloydev.sevibus.domain.model.LineColor
import com.sloydev.sevibus.domain.model.primary
import com.sloydev.sevibus.domain.model.soft

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = SevColors.Pink,
    onPrimary = Color.White,
    primaryContainer = SevColors.PinkSoft,
    onPrimaryContainer = SevColors.Neutral900,

    secondary = SevColors.Pink,
    onSecondary = Color.White,
    secondaryContainer = SevColors.PinkSoft,
    onSecondaryContainer = SevColors.Neutral900,

    tertiary = SevColors.Pink,
    onTertiary = Color.White,
    tertiaryContainer = SevColors.PinkSoft,
    onTertiaryContainer = SevColors.Neutral900,

    background = SevColors.Neutral0,
    onBackground = SevColors.Neutral900,

    surface = SevColors.Neutral50,
    onSurface = SevColors.Neutral900,
    onSurfaceVariant = SevColors.Neutral500,

    //surfaceVariant = ,
    //deprecated: surfaceTint = ,

//    inverseSurface = ,
//    inverseOnSurface = ,
    inversePrimary = SevColors.Pink,

//    error = ,
//    onError = ,
//    errorContainer = ,
//    onErrorContainer = ,

    outline = SevColors.OpacityMedium,
    outlineVariant = SevColors.OpacitySoft,

    //scrim = ,

    surfaceContainerLowest = SevColors.Neutral0,
    surfaceContainerLow = SevColors.Neutral50,
    surfaceContainer = SevColors.Neutral50,
    surfaceContainerHigh = SevColors.Neutral50,
    surfaceContainerHighest = SevColors.Neutral50,

    //surfaceBright = ,
    //surfaceDim = ,
)

@Composable
fun SevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object SevTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    /**
     * Retrieves the current [Typography] at the call site's position in the hierarchy.
     */
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /**
     * Retrieves the current [Shapes] at the call site's position in the hierarchy.
     */
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

