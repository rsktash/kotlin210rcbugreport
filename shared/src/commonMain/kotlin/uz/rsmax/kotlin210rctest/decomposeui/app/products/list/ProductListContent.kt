package uz.rsmax.kotlin210rctest.decomposeui.app.products.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.LoadingContent
import uz.rsmax.kotlin210rctest.NavBackButton
import uz.rsmax.kotlin210rctest.contentPadding
import uz.rsmax.kotlin210rctest.decompose.LoadableData.Companion.isLoadedAnd
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent
import uz.rsmax.kotlin210rctest.toStringIgnoreZero


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
  component: ProductListComponent,
  hideNavigationButton: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val state by component.model.subscribeAsState()

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        navigationIcon = {
          if (!hideNavigationButton) state.NavBackButton(component::onNavigateBack)
        },
        title = { Text(text = "Product list") },
        actions = {
          if (state.items.isLoadedAnd { isNotEmpty() }) {
            CreateNewButton(component)
          }
        }
      )
    },
    content = { paddingValues ->
      LoadingContent(state.items, Modifier.padding(paddingValues).contentPadding()) { products ->
        if (products.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CreateNewButton(component)
          }
        } else {
          LazyColumn {
            items(products) { product ->
              ListItem(
                headlineContent = { Text(text = product.name) },
                supportingContent = { Text(text = product.description) },
                trailingContent = { Text(text = product.price.toStringIgnoreZero()) },
                modifier = Modifier.clickable(onClick = { component.onSelectItem(product) })
              )
            }
          }
        }
      }
    },
  )
}

@Composable
private fun CreateNewButton(component: ProductListComponent) {
  TextButton(
    onClick = component::onClickNew,
    content = {
      Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
      Text(text = "Create new")
    },
  )
}