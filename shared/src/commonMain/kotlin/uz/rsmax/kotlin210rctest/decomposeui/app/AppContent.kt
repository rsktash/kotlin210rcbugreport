package uz.rsmax.kotlin210rctest.decomposeui.app

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import uz.rsmax.kotlin210rctest.NavigationSuiteScaffold
import uz.rsmax.kotlin210rctest.decompose.app.AppComponent
import uz.rsmax.kotlin210rctest.decompose.app.AppComponent.Child
import uz.rsmax.kotlin210rctest.decomposeui.app.dashboard.DashboardContent
import uz.rsmax.kotlin210rctest.decomposeui.app.products.ProductsPaneContent
import uz.rsmax.kotlin210rctest.decomposeui.icons.Dashboard
import uz.rsmax.kotlin210rctest.decomposeui.icons.IconProducts
import uz.rsmax.kotlin210rctest.showNavigation

@Composable
fun AppContent(
  component: AppComponent,
  modifier: Modifier = Modifier,
) {
  Children(component.stack) { currentStack ->
    val currentScreen = currentStack.instance
    val navigationBarVisible = currentScreen.navigationBarVisible()
    val navigationType = if (navigationBarVisible) run {
      val windowAdaptiveInfo = currentWindowAdaptiveInfo()
      NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
    } else NavigationSuiteType.None

    NavigationSuiteScaffold(
      layoutType = navigationType,
      modifier = modifier,
      navigationSuiteItems = {
        if (navigationType != NavigationSuiteType.NavigationBar) {
          item(
            selected = false,
            onClick = {},
            enabled = false,
            icon = { Spacer(Modifier.size(24.dp)) },
          )
        }
        item(
          selected = currentScreen is Child.Dashboard,
          onClick = component::onClickDashboard,
          icon = {
            Icon(
              imageVector = if (currentScreen is Child.Dashboard) Icons.Filled.Dashboard
              else Icons.Outlined.Dashboard,
              contentDescription = null,
            )
          },
          label = {
            Text(
              text = "Dashboard",
              textAlign = TextAlign.Center,
            )
          },
        )
        item(
          selected = currentScreen is Child.Products,
          onClick = component::onClickProducts,
          icon = {
            Icon(
              imageVector = if (currentScreen is Child.Products) Icons.Filled.IconProducts
              else Icons.Outlined.IconProducts,
              contentDescription = null,
            )
          },
          label = {
            Text(
              text = "Products",
              textAlign = TextAlign.Center,
            )
          },
        )
      },
      content = {
        when (currentScreen) {
          is Child.Dashboard ->
            DashboardContent(currentScreen.component, modifier)

          is Child.Products ->
            ProductsPaneContent(currentScreen.component, true, modifier)
        }
      },
    )
  }
}


@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun Child.navigationBarVisible(): Boolean {
  return when (val child = this) {
    is Child.Dashboard -> true

    is Child.Products -> child.component.panels.showNavigation()
  }
}