package uz.rsmax.kotlin210rctest.decompose.app.product.details

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigationOwner
import com.arkivanov.decompose.router.webhistory.ofStack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable
import uz.rsmax.kotlin210rctest.decompose.BaseRouterComponent
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductId
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductItem
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Child
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Configuration
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Output
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditComponent
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent
import uz.rsmax.kotlin210rctest.decompose.popElse

fun ProductDetailsComponent(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  navigateBack: () -> Unit,
): ProductDetailsComponent =
  ProductDetailsComponentImpl(componentContext, dependencies, navigateBack)


interface ProductDetailsComponent : BaseRouterComponent<Child>, WebHistoryNavigationOwner {
  sealed interface Child {
    data class View(val component: ProductViewComponent) : Child
    data class Edit(val component: ProductEditComponent) : Child
  }

  sealed interface Output {
    data class OnItemUpdated(val item: ProductItem) : Output
    data class OnItemCreated(val item: ProductItem) : Output
  }

  interface Dependencies {
    val output: suspend (Output) -> Unit
    val mode: Configuration
  }

  @Serializable
  sealed interface Configuration {
    @Serializable
    data class View(val id: ProductId) : Configuration

    @Serializable
    data class Edit(val id: ProductId) : Configuration

    @Serializable
    data object Create : Configuration
  }
}

internal class ProductDetailsComponentImpl(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  private val navigateBack: () -> Unit
) : ProductDetailsComponent,
  UserComponentContext by componentContext,
  Dependencies by dependencies {
  private val viewInput = MutableSharedFlow<ProductViewComponent.Input>(extraBufferCapacity = 1)
  private val navigation = StackNavigation<Configuration>()

  override val stack = childStack(
    source = navigation,
    serializer = Configuration.serializer(),
    initialStack = { listOf(mode) },
    childFactory = ::createChild,
  )

  override val webHistoryNavigation: WebHistoryNavigation<*> = WebHistoryNavigation.ofStack(
    navigator = navigation,
    stack = stack,
    serializer = Configuration.serializer(),
    pathMapper = {
      when (it.configuration) {
        is Configuration.Create -> "create"
        is Configuration.Edit -> "edit"
        is Configuration.View -> "view"
      }
    },
    parametersMapper = {
      when (val conf = it.configuration) {
        is Configuration.Create -> emptyMap()
        is Configuration.Edit -> mapOf("id" to conf.id.id.toString())
        is Configuration.View -> mapOf("id" to conf.id.id.toString())
      }
    }
  )


  private fun createChild(
    configuration: Configuration,
    context: UserComponentContext
  ): Child = when (configuration) {
    is Configuration.View -> Child.View(view(context, configuration.id))
    is Configuration.Create -> Child.Edit(createEdit(context))
    is Configuration.Edit -> Child.Edit(createEdit(context, configuration.id))
  }

  private fun view(
    context: UserComponentContext,
    id: ProductId,
  ): ProductViewComponent = ProductViewComponent(
    componentContext = context,
    dependencies = object : ProductViewComponent.Dependencies {
      override val productId: ProductId = id
      override val input: Flow<ProductViewComponent.Input> = viewInput
      override val output: suspend (ProductViewComponent.Output) -> Unit = { out ->
        when (out) {
          is ProductViewComponent.Output.OnEditItem ->
            navigation.bringToFront(Configuration.Edit(out.item.id))
        }
      }
    },
    navigateBack = { navigation.popElse(navigateBack) },
  )

  private fun createEdit(
    context: UserComponentContext,
    id: ProductId? = null,
  ): ProductEditComponent = ProductEditComponent(
    componentContext = context,
    dependencies = object : ProductEditComponent.Dependencies {
      override val itemId: ProductId? = id
      override val output: suspend (ProductEditComponent.Output) -> Unit = { out ->
        when (out) {
          is ProductEditComponent.Output.OnItemCreated -> {
            navigation.popElse(navigateBack)
            output(Output.OnItemCreated(out.item))
          }

          is ProductEditComponent.Output.OnItemUpdated -> {
            navigation.popElse(navigateBack)
            viewInput.emit(ProductViewComponent.Input.OnItemUpdated(out.item))
            output(Output.OnItemUpdated(out.item))
          }
        }
      }
    },
    navigateBack = { navigation.popElse(navigateBack) },
  )
}
