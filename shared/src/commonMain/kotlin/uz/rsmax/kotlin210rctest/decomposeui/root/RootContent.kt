package uz.rsmax.kotlin210rctest.decomposeui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import uz.rsmax.kotlin210rctest.decompose.root.RootComponent
import uz.rsmax.kotlin210rctest.decompose.root.RootComponent.Child
import uz.rsmax.kotlin210rctest.decomposeui.app.AppContent
import uz.rsmax.kotlin210rctest.decomposeui.login.LoginContent
import uz.rsmax.kotlin210rctest.decomposeui.splash.SplashContent

@Composable
fun RootContent(
  component: RootComponent,
  modifier: Modifier = Modifier,
) {
  Children(stack = component.stack, modifier = modifier) { stack ->
    when (val child = stack.instance) {
      is Child.Splash ->
        SplashContent(child.component)

      is Child.Login ->
        LoginContent(child.component)

      is Child.App ->
        AppContent(child.component)
    }
  }
}