package uz.rsmax.kotlin210rctest.decomposeui.app.products.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Child
import uz.rsmax.kotlin210rctest.decomposeui.app.products.details.edit.ProductEditContent
import uz.rsmax.kotlin210rctest.decomposeui.app.products.details.view.ProductViewContent

@Composable
fun ProductDetailsContent(
  component: ProductDetailsComponent,
  modifier: Modifier = Modifier,
) {
  Children(component.stack) { currentStack ->
    when (val child = currentStack.instance) {
      is Child.View -> ProductViewContent(child.component, modifier)
      is Child.Edit -> ProductEditContent(child.component, modifier)
    }
  }
}

