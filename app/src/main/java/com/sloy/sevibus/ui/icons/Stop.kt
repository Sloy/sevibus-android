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

public val SevIcons.Stop: ImageVector
    get() {
        if (_icStop24 != null) {
            return _icStop24!!
        }
        _icStop24 = Builder(
            name = "IcStop24", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(12.0f, 21.325f)
                curveTo(11.7667f, 21.325f, 11.5333f, 21.2833f, 11.3f, 21.2f)
                curveTo(11.0667f, 21.1167f, 10.8583f, 20.9917f, 10.675f, 20.825f)
                curveTo(9.5917f, 19.825f, 8.6333f, 18.85f, 7.8f, 17.9f)
                curveTo(6.9667f, 16.95f, 6.2708f, 16.0292f, 5.7125f, 15.1375f)
                curveTo(5.1542f, 14.2458f, 4.7292f, 13.3875f, 4.4375f, 12.5625f)
                curveTo(4.1458f, 11.7375f, 4.0f, 10.95f, 4.0f, 10.2f)
                curveTo(4.0f, 7.7f, 4.8042f, 5.7083f, 6.4125f, 4.225f)
                curveTo(8.0208f, 2.7417f, 9.8833f, 2.0f, 12.0f, 2.0f)
                curveTo(14.1167f, 2.0f, 15.9792f, 2.7417f, 17.5875f, 4.225f)
                curveTo(19.1958f, 5.7083f, 20.0f, 7.7f, 20.0f, 10.2f)
                curveTo(20.0f, 10.95f, 19.8542f, 11.7375f, 19.5625f, 12.5625f)
                curveTo(19.2708f, 13.3875f, 18.8458f, 14.2458f, 18.2875f, 15.1375f)
                curveTo(17.7292f, 16.0292f, 17.0333f, 16.95f, 16.2f, 17.9f)
                curveTo(15.3667f, 18.85f, 14.4083f, 19.825f, 13.325f, 20.825f)
                curveTo(13.1417f, 20.9917f, 12.9333f, 21.1167f, 12.7f, 21.2f)
                curveTo(12.4667f, 21.2833f, 12.2333f, 21.325f, 12.0f, 21.325f)
                close()
                moveTo(12.0f, 12.0f)
                curveTo(12.55f, 12.0f, 13.0208f, 11.8042f, 13.4125f, 11.4125f)
                curveTo(13.8042f, 11.0208f, 14.0f, 10.55f, 14.0f, 10.0f)
                curveTo(14.0f, 9.45f, 13.8042f, 8.9792f, 13.4125f, 8.5875f)
                curveTo(13.0208f, 8.1958f, 12.55f, 8.0f, 12.0f, 8.0f)
                curveTo(11.45f, 8.0f, 10.9792f, 8.1958f, 10.5875f, 8.5875f)
                curveTo(10.1958f, 8.9792f, 10.0f, 9.45f, 10.0f, 10.0f)
                curveTo(10.0f, 10.55f, 10.1958f, 11.0208f, 10.5875f, 11.4125f)
                curveTo(10.9792f, 11.8042f, 11.45f, 12.0f, 12.0f, 12.0f)
                close()
            }
        }
            .build()
        return _icStop24!!
    }

private var _icStop24: ImageVector? = null
