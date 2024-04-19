package com.sloydev.sevibus.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val SevIcons.DirectionsBusFill: ImageVector
    get() {
        if (_directionsBusFill != null) {
            return _directionsBusFill!!
        }
        _directionsBusFill = Builder(
            name = "DirectionsBusFill", defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp, viewportWidth = 960.0f, viewportHeight = 960.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(240.0f, 840.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
                reflectiveQuadTo(200.0f, 800.0f)
                verticalLineToRelative(-82.0f)
                quadToRelative(-18.0f, -20.0f, -29.0f, -44.5f)
                reflectiveQuadTo(160.0f, 620.0f)
                verticalLineToRelative(-380.0f)
                quadToRelative(0.0f, -83.0f, 77.0f, -121.5f)
                reflectiveQuadTo(480.0f, 80.0f)
                quadToRelative(172.0f, 0.0f, 246.0f, 37.0f)
                reflectiveQuadToRelative(74.0f, 123.0f)
                verticalLineToRelative(380.0f)
                quadToRelative(0.0f, 29.0f, -11.0f, 53.5f)
                reflectiveQuadTo(760.0f, 718.0f)
                verticalLineToRelative(82.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(720.0f, 840.0f)
                horizontalLineToRelative(-40.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
                reflectiveQuadTo(640.0f, 800.0f)
                verticalLineToRelative(-40.0f)
                lineTo(320.0f, 760.0f)
                verticalLineToRelative(40.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(280.0f, 840.0f)
                horizontalLineToRelative(-40.0f)
                close()
                moveTo(240.0f, 400.0f)
                horizontalLineToRelative(480.0f)
                verticalLineToRelative(-120.0f)
                lineTo(240.0f, 280.0f)
                verticalLineToRelative(120.0f)
                close()
                moveTo(340.0f, 640.0f)
                quadToRelative(25.0f, 0.0f, 42.5f, -17.5f)
                reflectiveQuadTo(400.0f, 580.0f)
                quadToRelative(0.0f, -25.0f, -17.5f, -42.5f)
                reflectiveQuadTo(340.0f, 520.0f)
                quadToRelative(-25.0f, 0.0f, -42.5f, 17.5f)
                reflectiveQuadTo(280.0f, 580.0f)
                quadToRelative(0.0f, 25.0f, 17.5f, 42.5f)
                reflectiveQuadTo(340.0f, 640.0f)
                close()
                moveTo(620.0f, 640.0f)
                quadToRelative(25.0f, 0.0f, 42.5f, -17.5f)
                reflectiveQuadTo(680.0f, 580.0f)
                quadToRelative(0.0f, -25.0f, -17.5f, -42.5f)
                reflectiveQuadTo(620.0f, 520.0f)
                quadToRelative(-25.0f, 0.0f, -42.5f, 17.5f)
                reflectiveQuadTo(560.0f, 580.0f)
                quadToRelative(0.0f, 25.0f, 17.5f, 42.5f)
                reflectiveQuadTo(620.0f, 640.0f)
                close()
            }
        }
            .build()
        return _directionsBusFill!!
    }

private var _directionsBusFill: ImageVector? = null
