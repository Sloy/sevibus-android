package com.sloy.sevibus.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.sloy.sevibus.R


object SevTypography {

    val headingLarge: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 36.sp,
    )

    val headingStandard: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    )

    val headingSmall: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )

    val bodyStandard: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val bodyStandardBold: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val bodySmall: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val bodySmallBold: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val bodyExtraSmall: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val bodyExtraSmallBold: TextStyle = DefaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
}

private val GeistFontFamily = FontFamily(
    Font(R.font.geist_light, FontWeight.Light),
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_bold, FontWeight.Bold),
    Font(R.font.geist_black, FontWeight.Black),
    Font(R.font.geist_extrabold, FontWeight.ExtraBold),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_thin, FontWeight.Thin),
    Font(R.font.geist_extralight, FontWeight.ExtraLight),
)

internal val DefaultTextStyle = TextStyle.Default.copy(
    fontFamily = GeistFontFamily,
    lineHeightStyle = LineHeightStyle.Default.copy(trim = LineHeightStyle.Trim.None)
)
