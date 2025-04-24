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

public val Linkout: ImageVector
	get() {
		if (_Linkout != null) {
			return _Linkout!!
		}
		_Linkout = ImageVector.Builder(
            name = "Linkout",
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
				moveTo(440f, 680f)
				horizontalLineTo(280f)
				quadToRelative(-83f, 0f, -141.5f, -58.5f)
				reflectiveQuadTo(80f, 480f)
				quadToRelative(0f, -83f, 58.5f, -141.5f)
				reflectiveQuadTo(280f, 280f)
				horizontalLineToRelative(160f)
				verticalLineToRelative(80f)
				horizontalLineTo(280f)
				quadToRelative(-50f, 0f, -85f, 35f)
				reflectiveQuadToRelative(-35f, 85f)
				quadToRelative(0f, 50f, 35f, 85f)
				reflectiveQuadToRelative(85f, 35f)
				horizontalLineToRelative(160f)
				verticalLineToRelative(80f)
				close()
				moveTo(320f, 520f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(320f)
				verticalLineToRelative(80f)
				horizontalLineTo(320f)
				close()
				moveToRelative(200f, 160f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(160f)
				quadToRelative(50f, 0f, 85f, -35f)
				reflectiveQuadToRelative(35f, -85f)
				quadToRelative(0f, -50f, -35f, -85f)
				reflectiveQuadToRelative(-85f, -35f)
				horizontalLineTo(520f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(160f)
				quadToRelative(83f, 0f, 141.5f, 58.5f)
				reflectiveQuadTo(880f, 480f)
				quadToRelative(0f, 83f, -58.5f, 141.5f)
				reflectiveQuadTo(680f, 680f)
				horizontalLineTo(520f)
				close()
			}
		}.build()
		return _Linkout!!
	}

private var _Linkout: ImageVector? = null
