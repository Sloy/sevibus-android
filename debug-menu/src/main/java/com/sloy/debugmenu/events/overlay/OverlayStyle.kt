package com.sloy.debugmenu.events.overlay

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Color
import com.sloy.debugmenu.events.EventType

private val Ink = Color(0xFF14151A)

internal object OverlayColors {
    val Glass = Ink.copy(alpha = 0.72f)
    val GlassMuted = Ink.copy(alpha = 0.5f)
    val OnGlass = Color.White
    val Badge = Color.White.copy(alpha = 0.16f)
    val TimerTrack = Color.White.copy(alpha = 0.08f)
    val RailTrack = Color.White.copy(alpha = 0.55f)
    val RailTrackBorder = Ink.copy(alpha = 0.08f)
    val RailLine = Ink.copy(alpha = 0.22f)
    val RailTick = Ink.copy(alpha = 0.35f)
    val RailTickText = Color(0xFF6B7080)
    val RailLink = Ink.copy(alpha = 0.55f)
    val MarkerRing = Color.White
    val MarkerShadow = Color.Black.copy(alpha = 0.3f)
    val RailBandFill = EventType.VIEW.color.copy(alpha = 0.13f)
    val RailBandBorder = EventType.VIEW.color.copy(alpha = 0.38f)
}

internal object OverlayEasing {
    val SpringyOut = CubicBezierEasing(0.30f, 1.35f, 0.45f, 1f)
    val PopOut = CubicBezierEasing(0.30f, 1.70f, 0.50f, 1f)
    val LabelOut = CubicBezierEasing(0.30f, 1.50f, 0.50f, 1f)
    val ExitAccel = CubicBezierEasing(0.30f, 0f, 0.80f, 0.15f)
    val CssEaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)
    val CssEaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    val CssEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
}
