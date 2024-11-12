package uz.rsmax.kotlin210rctest.decomposeui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

val Icons.Outlined.IconProducts: ImageVector
  get() {
    if (_products != null) {
      return _products!!
    }
    _products =
        ImageVector.Builder(
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 48f,
                viewportHeight = 48f,
            )
            .addPath(
                pathData =
                    addPathNodes(
                        "m45.3 12.8-21-10.5a.8.8 0 0 0-.6 0l-21 10.5c-.3.2-.5.4-.5.7v21c0 .3.2.5.5.7l21 10.5h.6l21-10.5c.3-.2.5-.4.5-.7v-21c0-.3-.2-.5-.5-.7ZM24 23.2l-5.9-3 18.8-10 6.4 3.3Zm-11.9-6L31 7.2l4.3 2.2-18.7 10Zm-.8 1.3 4.4 2.2v6L14 25a.8.8 0 0 0-.5-.3h-2.3ZM24 3.8l5.2 2.6-18.7 10-5.8-2.9Zm-20.3 11 6 3v7.7c0 .4.4.8.8.8h2.7L16 29c.2.2.5.3.8.2.3-.1.4-.4.4-.7v-7l6 3v19.3L3.9 34Zm21 29V24.5l19.6-9.8V34Zm0 0"),
                fill = SolidColor(Color(0xff000000)),
            )
            .build()
    return _products!!
  }

private var _products: ImageVector? = null
