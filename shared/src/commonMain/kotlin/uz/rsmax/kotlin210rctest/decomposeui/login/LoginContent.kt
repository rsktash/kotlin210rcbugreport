package uz.rsmax.kotlin210rctest.decomposeui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.contentPadding
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent
import uz.rsmax.kotlin210rctest.rememberTextFieldStateAndUpdate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
  component: LoginComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.model.subscribeAsState()
  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(text = "Login")
        },
      )
    },
    content = { paddingValues ->
      Box(Modifier.padding(paddingValues).contentPadding().fillMaxWidth(), Alignment.Center) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            state = rememberTextFieldStateAndUpdate(state.user.name, component::onEditName),
            label = { Text(text = "Name") },
            labelPosition = TextFieldLabelPosition.Above(),
            placeholder = { Text(text = "Enter your name") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              capitalization = KeyboardCapitalization.Words,
              imeAction = ImeAction.Next,
            ),
          )

          OutlinedTextField(
            state = rememberTextFieldStateAndUpdate(state.user.email, component::onEditEmail),
            label = { Text(text = "Email") },
            labelPosition = TextFieldLabelPosition.Above(),
            placeholder = { Text(text = "Enter your email") },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              capitalization = KeyboardCapitalization.Words,
              imeAction = ImeAction.Next,
            ),
          )
          AnimatedVisibility(state.isDataValid) {
            TextButton(
              onClick = component::onSingIn,
              content = { Text(text = "Sign in") },
            )
          }
        }
      }
    },
  )
}