package uz.rsmax.kotlin210rctest.decomposeui.app.products.details.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.base.EditableData
import uz.rsmax.kotlin210rctest.EditingContent
import uz.rsmax.kotlin210rctest.NavBackButton
import uz.rsmax.kotlin210rctest.contentPadding
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditComponent
import uz.rsmax.kotlin210rctest.decomposeui.component.DecimalTextField
import uz.rsmax.kotlin210rctest.rememberTextFieldStateAndUpdate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditContent(
  component: ProductEditComponent,
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
        title = {
          val title = when (val item = state.item) {
            is EditableData.Idle,
            is EditableData.Error -> "Edit product"

            is EditableData.Loaded -> {
              if (item.data.id.isNew()) {
                "Create new Product"
              } else "Edit product"
            }
          }
          Text(text = title)
        },
      )
    },
    content = { paddingValues ->
      EditingContent(
        state.item,
        component::onDisposeChanges,
        component::onNavigateBack,
        Modifier.padding(paddingValues).contentPadding(),
      ) { item ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

          OutlinedTextField(
            state = rememberTextFieldStateAndUpdate(item.name, component::onEditName),
            label = { Text(text = "Name") },
            labelPosition = TextFieldLabelPosition.Above(),
            placeholder = { Text(text = "Enter product name") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
          )
          OutlinedTextField(
            state = rememberTextFieldStateAndUpdate(item.description, component::onEditDescription),
            label = { Text(text = "Description") },
            labelPosition = TextFieldLabelPosition.Above(),
            placeholder = { Text(text = "Enter product description") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
          )
          DecimalTextField(
            value = item.price,
            onValueChange = component::onEditPrice,
            label = { Text(text = "Price") },
            labelPosition = TextFieldLabelPosition.Above(),
            placeholder = { Text(text = "Enter product price") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Next
            ),
          )
          TextButton(
            onClick = component::onSave,
            content = { Text(text = "Save") },
            enabled = state.isDataValid,
          )
        }
      }
    },
  )
}