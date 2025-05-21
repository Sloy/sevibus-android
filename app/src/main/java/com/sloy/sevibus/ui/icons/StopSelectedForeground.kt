package com.sloy.sevibus.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val SevIcons.StopSelectedForeground: ImageVector
    get() {
        if (_icStopSelectedForeground40 != null) {
            return _icStopSelectedForeground40!!
        }
        _icStopSelectedForeground40 = Builder(
            name = "IcStopSelectedForeground40", defaultWidth =
            40.0.dp, defaultHeight = 40.0.dp, viewportWidth = 40.0f, viewportHeight =
            40.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.0f, 35.5416f)
                curveTo(19.6111f, 35.5416f, 19.2222f, 35.4722f, 18.8333f, 35.3333f)
                curveTo(18.4444f, 35.1944f, 18.0972f, 34.9861f, 17.7916f, 34.7083f)
                curveTo(15.9861f, 33.0416f, 14.3888f, 31.4167f, 13.0f, 29.8333f)
                curveTo(11.6111f, 28.25f, 10.4513f, 26.7153f, 9.5208f, 25.2291f)
                curveTo(8.5902f, 23.743f, 7.8819f, 22.3125f, 7.3958f, 20.9375f)
                curveTo(6.9097f, 19.5625f, 6.6666f, 18.25f, 6.6666f, 17.0f)
                curveTo(6.6666f, 12.8333f, 8.0069f, 9.5139f, 10.6875f, 7.0416f)
                curveTo(13.368f, 4.5694f, 16.4722f, 3.3333f, 20.0f, 3.3333f)
                curveTo(23.5277f, 3.3333f, 26.6319f, 4.5694f, 29.3125f, 7.0416f)
                curveTo(31.993f, 9.5139f, 33.3333f, 12.8333f, 33.3333f, 17.0f)
                curveTo(33.3333f, 18.25f, 33.0902f, 19.5625f, 32.6041f, 20.9375f)
                curveTo(32.118f, 22.3125f, 31.4097f, 23.743f, 30.4791f, 25.2291f)
                curveTo(29.5486f, 26.7153f, 28.3888f, 28.25f, 27.0f, 29.8333f)
                curveTo(25.6111f, 31.4167f, 24.0138f, 33.0416f, 22.2083f, 34.7083f)
                curveTo(21.9027f, 34.9861f, 21.5555f, 35.1944f, 21.1666f, 35.3333f)
                curveTo(20.7777f, 35.4722f, 20.3888f, 35.5416f, 20.0f, 35.5416f)
                close()
                moveTo(20.0f, 20.0f)
                curveTo(20.9166f, 20.0f, 21.7013f, 19.6736f, 22.3541f, 19.0208f)
                curveTo(23.0069f, 18.368f, 23.3333f, 17.5833f, 23.3333f, 16.6666f)
                curveTo(23.3333f, 15.75f, 23.0069f, 14.9653f, 22.3541f, 14.3125f)
                curveTo(21.7013f, 13.6597f, 20.9166f, 13.3333f, 20.0f, 13.3333f)
                curveTo(19.0833f, 13.3333f, 18.2986f, 13.6597f, 17.6458f, 14.3125f)
                curveTo(16.993f, 14.9653f, 16.6666f, 15.75f, 16.6666f, 16.6666f)
                curveTo(16.6666f, 17.5833f, 16.993f, 18.368f, 17.6458f, 19.0208f)
                curveTo(18.2986f, 19.6736f, 19.0833f, 20.0f, 20.0f, 20.0f)
                close()
            }
        }
            .build()
        return _icStopSelectedForeground40!!
    }

private var _icStopSelectedForeground40: ImageVector? = null
