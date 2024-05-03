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

public val SevIcons.Home: ImageVector
    get() {
        if (_icHome24 != null) {
            return _icHome24!!
        }
        _icHome24 = Builder(
            name = "IcHome24", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(4.0f, 19.0f)
                verticalLineTo(10.0f)
                curveTo(4.0f, 9.6833f, 4.0708f, 9.3833f, 4.2125f, 9.1f)
                curveTo(4.3542f, 8.8167f, 4.55f, 8.5833f, 4.8f, 8.4f)
                lineTo(10.8f, 3.9f)
                curveTo(11.15f, 3.6333f, 11.55f, 3.5f, 12.0f, 3.5f)
                curveTo(12.45f, 3.5f, 12.85f, 3.6333f, 13.2f, 3.9f)
                lineTo(19.2f, 8.4f)
                curveTo(19.45f, 8.5833f, 19.6458f, 8.8167f, 19.7875f, 9.1f)
                curveTo(19.9292f, 9.3833f, 20.0f, 9.6833f, 20.0f, 10.0f)
                verticalLineTo(19.0f)
                curveTo(20.0f, 19.55f, 19.8042f, 20.0208f, 19.4125f, 20.4125f)
                curveTo(19.0208f, 20.8042f, 18.55f, 21.0f, 18.0f, 21.0f)
                horizontalLineTo(15.0f)
                curveTo(14.7167f, 21.0f, 14.4792f, 20.9042f, 14.2875f, 20.7125f)
                curveTo(14.0958f, 20.5208f, 14.0f, 20.2833f, 14.0f, 20.0f)
                verticalLineTo(15.0f)
                curveTo(14.0f, 14.7167f, 13.9042f, 14.4792f, 13.7125f, 14.2875f)
                curveTo(13.5208f, 14.0958f, 13.2833f, 14.0f, 13.0f, 14.0f)
                horizontalLineTo(11.0f)
                curveTo(10.7167f, 14.0f, 10.4792f, 14.0958f, 10.2875f, 14.2875f)
                curveTo(10.0958f, 14.4792f, 10.0f, 14.7167f, 10.0f, 15.0f)
                verticalLineTo(20.0f)
                curveTo(10.0f, 20.2833f, 9.9042f, 20.5208f, 9.7125f, 20.7125f)
                curveTo(9.5208f, 20.9042f, 9.2833f, 21.0f, 9.0f, 21.0f)
                horizontalLineTo(6.0f)
                curveTo(5.45f, 21.0f, 4.9792f, 20.8042f, 4.5875f, 20.4125f)
                curveTo(4.1958f, 20.0208f, 4.0f, 19.55f, 4.0f, 19.0f)
                close()
            }
        }
            .build()
        return _icHome24!!
    }

private var _icHome24: ImageVector? = null
