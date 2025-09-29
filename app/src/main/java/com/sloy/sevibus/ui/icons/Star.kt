package com.sloy.sevibus.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val SevIcons.Star: ImageVector
    get() {
        if (_star != null) {
            return _star!!
        }
        _star = Builder(
            name =
                "Star24dp000000Fill1Wght400Grad0Opsz24", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 960.0f, viewportHeight = 960.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(480.0f, 691.0f)
                lineTo(314.0f, 791.0f)
                quadToRelative(-11.0f, 7.0f, -23.0f, 6.0f)
                reflectiveQuadToRelative(-21.0f, -8.0f)
                quadToRelative(-9.0f, -7.0f, -14.0f, -17.5f)
                reflectiveQuadToRelative(-2.0f, -23.5f)
                lineToRelative(44.0f, -189.0f)
                lineToRelative(-147.0f, -127.0f)
                quadToRelative(-10.0f, -9.0f, -12.5f, -20.5f)
                reflectiveQuadTo(140.0f, 389.0f)
                quadToRelative(4.0f, -11.0f, 12.0f, -18.0f)
                reflectiveQuadToRelative(22.0f, -9.0f)
                lineToRelative(194.0f, -17.0f)
                lineToRelative(75.0f, -178.0f)
                quadToRelative(5.0f, -12.0f, 15.5f, -18.0f)
                reflectiveQuadToRelative(21.5f, -6.0f)
                quadToRelative(11.0f, 0.0f, 21.5f, 6.0f)
                reflectiveQuadToRelative(15.5f, 18.0f)
                lineToRelative(75.0f, 178.0f)
                lineToRelative(194.0f, 17.0f)
                quadToRelative(14.0f, 2.0f, 22.0f, 9.0f)
                reflectiveQuadToRelative(12.0f, 18.0f)
                quadToRelative(4.0f, 11.0f, 1.5f, 22.5f)
                reflectiveQuadTo(809.0f, 432.0f)
                lineTo(662.0f, 559.0f)
                lineToRelative(44.0f, 189.0f)
                quadToRelative(3.0f, 13.0f, -2.0f, 23.5f)
                reflectiveQuadTo(690.0f, 789.0f)
                quadToRelative(-9.0f, 7.0f, -21.0f, 8.0f)
                reflectiveQuadToRelative(-23.0f, -6.0f)
                lineTo(480.0f, 691.0f)
                close()
            }
        }
            .build()
        return _star!!
    }

private var _star: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = SevIcons.Star, contentDescription = "")
    }
}
