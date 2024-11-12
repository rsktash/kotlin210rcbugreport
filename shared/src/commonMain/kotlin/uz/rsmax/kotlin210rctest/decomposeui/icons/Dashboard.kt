package uz.rsmax.kotlin210rctest.decomposeui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

val Icons.Outlined.Dashboard: ImageVector
  get() {
    if (_dashboard != null) {
      return _dashboard!!
    }
    _dashboard =
        ImageVector.Builder(
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 48f,
                viewportHeight = 48f,
            )
            .addPath(
                pathData =
                    addPathNodes(
                        "M19.4 17.4H7a2.9 2.9 0 0 1-2.9-3V7c0-1.6 1.3-2.9 3-2.9h12.3c1.6 0 3 1.3 3 3v7.4c0 1.6-1.4 2.9-3 2.9ZM7 6.6c-.2 0-.4.2-.4.4v7.5c0 .2.2.4.4.4h12.4c.3 0 .5-.2.5-.4V7c0-.2-.2-.4-.5-.4ZM19.4 44H7a2.9 2.9 0 0 1-2.9-3V23.7c0-1.6 1.3-3 3-3h12.3c1.6 0 3 1.4 3 3V41c0 1.6-1.4 2.9-3 2.9ZM7 23.2c-.2 0-.4.2-.4.4V41c0 .2.2.4.4.4h12.4c.3 0 .5-.2.5-.4V23.6c0-.2-.2-.4-.5-.4Zm34 20.7H28.6a2.9 2.9 0 0 1-3-3v-7.4c0-1.6 1.4-2.9 3-2.9H41c1.6 0 2.9 1.3 2.9 3V41c0 1.6-1.3 2.9-3 2.9ZM28.6 33c-.3 0-.5.2-.5.4V41c0 .2.2.4.5.4H41c.2 0 .4-.2.4-.4v-7.5c0-.2-.2-.4-.4-.4ZM41 27.3H28.6a2.9 2.9 0 0 1-3-2.9V7c0-1.6 1.4-2.9 3-2.9H41c1.6 0 2.9 1.3 2.9 3v17.3c0 1.6-1.3 3-3 3ZM28.6 6.6c-.3 0-.5.2-.5.4v17.4c0 .2.2.4.5.4H41c.2 0 .4-.2.4-.4V7c0-.2-.2-.4-.4-.4Zm0 0"),
                fill = SolidColor(Color(0xff000000)),
            )
            .build()
    return _dashboard!!
  }

private var _dashboard: ImageVector? = null
