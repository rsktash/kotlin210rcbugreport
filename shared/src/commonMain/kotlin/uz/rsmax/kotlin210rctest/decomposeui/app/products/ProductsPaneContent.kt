package uz.rsmax.kotlin210rctest.decomposeui.app.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.ChildPanels
import uz.rsmax.kotlin210rctest.decompose.app.product.pane.ProductMultiPaneComponent
import uz.rsmax.kotlin210rctest.decomposeui.app.products.details.ProductDetailsContent
import uz.rsmax.kotlin210rctest.decomposeui.app.products.list.ProductListContent
import uz.rsmax.kotlin210rctest.isMultiPane
import uz.rsmax.kotlin210rctest.rememberHorizontalChildPanelsLayout
import uz.rsmax.kotlin210rctest.rememberPredictiveBackParams

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ProductsPaneContent(
  component: ProductMultiPaneComponent,
  hideNavigationButton: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val panels by component.panels.subscribeAsState()

  val isMultiPane = isMultiPane()
  LaunchedEffect(isMultiPane) { component.setMultiPane(isMultiPane) }

  ChildPanels(
    modifier = modifier,
    panels = panels,
    mainChild = { ProductListContent(it.instance, hideNavigationButton) },
    detailsChild = { ProductDetailsContent(it.instance) },
    layout = rememberHorizontalChildPanelsLayout(),
    predictiveBackParams = rememberPredictiveBackParams(component),
  )

}