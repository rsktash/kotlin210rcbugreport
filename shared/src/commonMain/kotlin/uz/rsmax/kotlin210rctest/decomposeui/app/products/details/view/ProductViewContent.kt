package uz.rsmax.kotlin210rctest.decomposeui.app.products.details.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.LabeledText
import uz.rsmax.kotlin210rctest.LoadingContent
import uz.rsmax.kotlin210rctest.NavBackButton
import uz.rsmax.kotlin210rctest.contentPadding
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductViewContent(
  component: ProductViewComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.model.subscribeAsState()
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        navigationIcon = {
          state.NavBackButton(component::onNavigateBack)
        },
        title = { Text(text = "Product view") },
        actions = {
          TextButton(
            onClick = component::onEditItem,
            content = { Text(text = "Edit") },
          )
        },
      )
    },
    content = { paddingValues ->
      LoadingContent(state.item, Modifier.padding(paddingValues).contentPadding()) { item ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          LabeledText(
            label = "Name",
            text = { Text(text = item.name) }
          )
          LabeledText(
            label = "Description",
            text = { Text(text = item.description) },
          )

          LabeledText(
            label = "Price",
            text = { Text(text = item.price.toString()) },
          )
        }
      }
    },
  )
}