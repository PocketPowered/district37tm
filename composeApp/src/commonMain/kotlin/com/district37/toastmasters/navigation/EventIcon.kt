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

val EventIcon: ImageVector
	get() {
		if (_eventIcon != null) {
			return _eventIcon!!
		}
		_eventIcon = ImageVector.Builder(
            name = "Event_24dp_E8EAED_FILL0_wght400_GRAD0_opsz24",
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
				moveTo(580f, 720f)
				quadToRelative(-42f, 0f, -71f, -29f)
				reflectiveQuadToRelative(-29f, -71f)
				quadToRelative(0f, -42f, 29f, -71f)
				reflectiveQuadToRelative(71f, -29f)
				quadToRelative(42f, 0f, 71f, 29f)
				reflectiveQuadToRelative(29f, 71f)
				quadToRelative(0f, 42f, -29f, 71f)
				reflectiveQuadToRelative(-71f, 29f)
				close()
				moveTo(200f, 880f)
				quadToRelative(-33f, 0f, -56.5f, -23.5f)
				reflectiveQuadTo(120f, 800f)
				verticalLineToRelative(-560f)
				quadToRelative(0f, -33f, 23.5f, -56.5f)
				reflectiveQuadTo(200f, 160f)
				horizontalLineToRelative(40f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(320f)
				verticalLineToRelative(-80f)
				horizontalLineToRelative(80f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(40f)
				quadToRelative(33f, 0f, 56.5f, 23.5f)
				reflectiveQuadTo(840f, 240f)
				verticalLineToRelative(560f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(760f, 880f)
				horizontalLineTo(200f)
				close()
				moveToRelative(0f, -80f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-400f)
				horizontalLineTo(200f)
				verticalLineToRelative(400f)
				close()
				moveToRelative(0f, -480f)
				horizontalLineToRelative(560f)
				verticalLineToRelative(-80f)
				horizontalLineTo(200f)
				verticalLineToRelative(80f)
				close()
				moveToRelative(0f, 0f)
				verticalLineToRelative(-80f)
				verticalLineToRelative(80f)
				close()
			}
		}.build()
		return _eventIcon!!
	}

private var _eventIcon: ImageVector? = null
