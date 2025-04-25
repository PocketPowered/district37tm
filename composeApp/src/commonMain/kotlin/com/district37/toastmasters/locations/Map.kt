/*
* Converted using https://composables.com/svgtocompose
*/

package com.district37.toastmasters.locations

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Map: ImageVector
	get() {
		if (_Map != null) {
			return _Map!!
		}
		_Map = ImageVector.Builder(
            name = "Map",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
			path(
    			fill = SolidColor(Color(0xFFE8EAED)),
    			fillAlpha = 1.0f,
    			stroke = null,
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.0f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(600f, 840f)
				lineToRelative(-240f, -84f)
				lineToRelative(-186f, 72f)
				quadToRelative(-20f, 8f, -37f, -4.5f)
				reflectiveQuadTo(120f, 790f)
				verticalLineToRelative(-560f)
				quadToRelative(0f, -13f, 7.5f, -23f)
				reflectiveQuadToRelative(20.5f, -15f)
				lineToRelative(212f, -72f)
				lineToRelative(240f, 84f)
				lineToRelative(186f, -72f)
				quadToRelative(20f, -8f, 37f, 4.5f)
				reflectiveQuadToRelative(17f, 33.5f)
				verticalLineToRelative(560f)
				quadToRelative(0f, 13f, -7.5f, 23f)
				reflectiveQuadTo(812f, 768f)
				lineToRelative(-212f, 72f)
				close()
				moveToRelative(-40f, -98f)
				verticalLineToRelative(-468f)
				lineToRelative(-160f, -56f)
				verticalLineToRelative(468f)
				lineToRelative(160f, 56f)
				close()
				moveToRelative(80f, 0f)
				lineToRelative(120f, -40f)
				verticalLineToRelative(-474f)
				lineToRelative(-120f, 46f)
				verticalLineToRelative(468f)
				close()
				moveToRelative(-440f, -10f)
				lineToRelative(120f, -46f)
				verticalLineToRelative(-468f)
				lineToRelative(-120f, 40f)
				verticalLineToRelative(474f)
				close()
				moveToRelative(440f, -458f)
				verticalLineToRelative(468f)
				verticalLineToRelative(-468f)
				close()
				moveToRelative(-320f, -56f)
				verticalLineToRelative(468f)
				verticalLineToRelative(-468f)
				close()
			}
		}.build()
		return _Map!!
	}

private var _Map: ImageVector? = null
