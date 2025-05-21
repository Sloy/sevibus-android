/*
* Converted using https://composables.com/svgtocompose
*/

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.icons.SevIcons

public val SevIcons.Chevron: ImageVector
	get() {
		if (_Chevron != null) {
			return _Chevron!!
		}
		_Chevron = ImageVector.Builder(
            name = "Chevron",
            defaultWidth = 14.dp,
            defaultHeight = 12.dp,
            viewportWidth = 14f,
            viewportHeight = 12f
        ).apply {
			path(
    			fill = SolidColor(Color(0xFF000000)),
    			fillAlpha = 1.0f,
    			stroke = null,
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.0f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(4.30617f, 9.46225f)
				curveTo(5.20250f, 10.88340f, 5.65060f, 11.5940f, 6.22070f, 11.83930f)
				curveTo(6.71890f, 12.05360f, 7.28110f, 12.05360f, 7.77930f, 11.83930f)
				curveTo(8.34940f, 11.5940f, 8.79750f, 10.88340f, 9.69380f, 9.46220f)
				lineTo(12.4998f, 5.01309f)
				curveTo(13.52980f, 3.380f, 14.04480f, 2.56340f, 13.99690f, 1.88830f)
				curveTo(13.95520f, 1.29990f, 13.66450f, 0.75890f, 13.20020f, 0.40550f)
				curveTo(12.66730f, 00f, 11.71360f, 00f, 9.8060f, 00f)
				horizontalLineTo(4.19398f)
				curveTo(2.28640f, 00f, 1.33270f, 00f, 0.79980f, 0.40550f)
				curveTo(0.33550f, 0.75890f, 0.04480f, 1.29990f, 0.00310f, 1.88830f)
				curveTo(-0.04480f, 2.56340f, 0.47020f, 3.380f, 1.50020f, 5.01310f)
				lineTo(4.30617f, 9.46225f)
				close()
			}
		}.build()
		return _Chevron!!
	}

private var _Chevron: ImageVector? = null
