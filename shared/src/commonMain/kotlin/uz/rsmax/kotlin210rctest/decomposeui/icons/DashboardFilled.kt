package uz.rsmax.kotlin210rctest.decomposeui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

public val Icons.Filled.Dashboard: ImageVector
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
                        "M19.4 4.1H7a2.9 2.9 0 0 0-2.9 3v7.4c0 1.6 1.3 2.9 3 2.9h12.3c1.6 0 3-1.3 3-3V7c0-1.6-1.4-2.9-3-2.9Zm0 16.6H7a2.9 2.9 0 0 0-2.9 2.9V41c0 1.6 1.3 2.9 3 2.9h12.3c1.6 0 3-1.3 3-3V23.7c0-1.6-1.4-3-3-3Zm21.6 10H28.6a2.9 2.9 0 0 0-3 2.8V41c0 1.6 1.4 2.9 3 2.9H41c1.6 0 2.9-1.3 2.9-3v-7.4c0-1.6-1.3-2.9-3-2.9ZM41 4H28.6a2.9 2.9 0 0 0-3 3v17.3c0 1.6 1.4 3 3 3H41c1.6 0 2.9-1.4 2.9-3V7c0-1.6-1.3-2.9-3-2.9Zm0 0"),
                fill = SolidColor(Color(0xff000000)),
            )
            .build()
    return _dashboard!!
  }

private var _dashboard: ImageVector? = null
