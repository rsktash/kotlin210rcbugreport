package uz.rsmax.kotlin210rctest.decompose.app

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigationOwner
import com.arkivanov.decompose.router.webhistory.ofStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import uz.rsmax.kotlin210rctest.decompose.BaseRouterComponent
import uz.rsmax.kotlin210rctest.decompose.app.AppComponent.Child
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardComponent
import uz.rsmax.kotlin210rctest.decompose.app.product.pane.ProductMultiPaneComponent

fun AppComponent(
  componentContext: UserComponentContext
): AppComponent = AppComponentImpl(componentContext)

interface AppComponent : BaseRouterComponent<Child>, WebHistoryNavigationOwner {
  sealed interface Child {
    data class Dashboard(val component: DashboardComponent) : Child
    data class Products(val component: ProductMultiPaneComponent) : Child
  }

  fun onClickDashboard()
  fun onClickProducts()
}

internal class AppComponentImpl(
  componentContext: UserComponentContext,
) : AppComponent,
  UserComponentContext by componentContext {

  private val navigation = StackNavigation<Configuration>()

  private val _stack = childStack(
    source = navigation,
    serializer = Configuration.serializer(),
    initialStack = { listOf(Configuration.Dashboard) },
    handleBackButton = false,
    childFactory = ::createChild,
  )

  override val stack: Value<ChildStack<*, Child>> = _stack

  override val webHistoryNavigation: WebHistoryNavigation<*> = WebHistoryNavigation.ofStack(
    navigator = navigation,
    stack = _stack,
    serializer = Configuration.serializer(),
    isHistoryEnabled = false,
    pathMapper = {
      when (it.configuration) {
        Configuration.Dashboard -> "dashboard"
        Configuration.Products -> "products"
      }
    },
    childLocator = {
      when (val child = it.instance) {
        is Child.Dashboard -> null
        is Child.Products -> child.component
      }
    }
  )

  private fun createChild(
    configuration: Configuration,
    context: UserComponentContext
  ): Child = when (configuration) {
    Configuration.Dashboard -> Child.Dashboard(dashboard(context))
    Configuration.Products -> Child.Products(products(context))
  }

  private fun dashboard(context: UserComponentContext): DashboardComponent =
    DashboardComponent(
      componentContext = context,
    )

  private fun products(context: UserComponentContext): ProductMultiPaneComponent =
    ProductMultiPaneComponent(
      componentContext = context,
    )

  override fun onClickDashboard() =
    navigation.bringToFront(Configuration.Dashboard)

  override fun onClickProducts() =
    navigation.bringToFront(Configuration.Products)


  @Serializable
  private sealed interface Configuration {
    @Serializable
    data object Dashboard : Configuration

    @Serializable
    data object Products : Configuration
  }

}