package uz.rsmax.kotlin210rctest.decomposeui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

val Icons.Filled.IconProducts: ImageVector
  get() {
    if (_applications != null) {
      return _applications!!
    }
    _applications =
        ImageVector.Builder(
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 48f,
                viewportHeight = 48f,
            )
            .addPath(
                pathData =
                    addPathNodes(
                        "M11.1 17 31 6.5 24 3 3.6 13.3ZM37 9.5 17 20l6.9 3.5 20.4-10.2Zm-13.5 15L16.5 21v7.7l-3-3h-3V18L3 14.2v20.5L23.4 45Zm1.2 0V45L45 34.7V14.2Zm0 0"),
                fill = SolidColor(Color(0xff000000)),
            )
            .build()
    return _applications!!
  }

private var _applications: ImageVector? = null
