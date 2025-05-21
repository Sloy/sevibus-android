package myiconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.icons.SevIcons

public val SevIcons.StopOutline: ImageVector
    get() {
        if (_icStopOutline40 != null) {
            return _icStopOutline40!!
        }
        _icStopOutline40 = Builder(
            name = "IcStopOutline40", defaultWidth = 24.0.dp, defaultHeight =
            24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(12.0f, 24.0f)
                curveTo(11.7083f, 24.0f, 11.4167f, 23.9483f, 11.125f, 23.8448f)
                curveTo(10.8333f, 23.7413f, 10.5729f, 23.586f, 10.3438f, 23.379f)
                curveTo(8.9896f, 22.1371f, 7.7917f, 20.9263f, 6.75f, 19.7464f)
                curveTo(5.7083f, 18.5666f, 4.8385f, 17.423f, 4.1406f, 16.3157f)
                curveTo(3.4427f, 15.2083f, 2.9115f, 14.1423f, 2.5469f, 13.1177f)
                curveTo(2.1823f, 12.0931f, 2.0f, 11.1151f, 2.0f, 10.1837f)
                curveTo(2.0f, 7.0789f, 3.0052f, 4.6054f, 5.0156f, 2.7633f)
                curveTo(7.026f, 0.9211f, 9.3542f, 0.0f, 12.0f, 0.0f)
                curveTo(14.6458f, 0.0f, 16.974f, 0.9211f, 18.9844f, 2.7633f)
                curveTo(20.9948f, 4.6054f, 22.0f, 7.0789f, 22.0f, 10.1837f)
                curveTo(22.0f, 11.1151f, 21.8177f, 12.0931f, 21.4531f, 13.1177f)
                curveTo(21.0885f, 14.1423f, 20.5573f, 15.2083f, 19.8594f, 16.3157f)
                curveTo(19.1615f, 17.423f, 18.2917f, 18.5666f, 17.25f, 19.7464f)
                curveTo(16.2083f, 20.9263f, 15.0104f, 22.1371f, 13.6562f, 23.379f)
                curveTo(13.4271f, 23.586f, 13.1667f, 23.7413f, 12.875f, 23.8448f)
                curveTo(12.5833f, 23.9483f, 12.2917f, 24.0f, 12.0f, 24.0f)
                close()
            }
        }
            .build()
        return _icStopOutline40!!
    }

private var _icStopOutline40: ImageVector? = null
