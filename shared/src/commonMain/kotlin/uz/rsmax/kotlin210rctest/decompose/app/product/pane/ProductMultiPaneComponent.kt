package uz.rsmax.kotlin210rctest.decompose.app.product.pane

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigationOwner
import com.arkivanov.decompose.router.webhistory.ofPanels
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.mvikotlin.core.rx.internal.BehaviorSubject
import com.arkivanov.mvikotlin.core.utils.internal.InternalMviKotlinApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
import uz.rsmax.kotlin210rctest.decompose.BaseNavBackEvents
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent
import uz.rsmax.kotlin210rctest.decompose.app.product.details.ProductDetailsComponent.Configuration
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent
import uz.rsmax.kotlin210rctest.decompose.popElse
import uz.rsmax.kotlin210rctest.decompose.setMultiPane

fun ProductMultiPaneComponent(
  componentContext: UserComponentContext,
  navigateBack: (() -> Unit)? = null,
): ProductMultiPaneComponent = ProductMultiPaneComponentImpl(componentContext, navigateBack)


interface ProductMultiPaneComponent : BackHandlerOwner, BaseNavBackEvents,
  WebHistoryNavigationOwner {
  val panels: Value<ChildPanels<*, ProductListComponent, *, ProductDetailsComponent, Nothing, Nothing>>

  fun setMultiPane(isMultiPane: Boolean)
}

@OptIn(
  ExperimentalDecomposeApi::class,
  InternalMviKotlinApi::class,
  ExperimentalSerializationApi::class,
)
internal class ProductMultiPaneComponentImpl(
  componentContext: UserComponentContext,
  private val navigateBack: (() -> Unit)? = null,
) : ProductMultiPaneComponent,
  UserComponentContext by componentContext {

  private val listInput = MutableSharedFlow<ProductListComponent.Input>(extraBufferCapacity = 1)

  private val nav: PanelsNavigation<Unit, Configuration, Nothing> = PanelsNavigation()

  private val navState: BehaviorSubject<Panels<Unit, Configuration, Nothing>?> =
    BehaviorSubject(null)

  override val panels = childPanels(
    source = nav,
    initialPanels = { Panels(main = Unit) },
    serializers = Unit.serializer() to Configuration.serializer(),
    onStateChanged = { newState, _ -> navState.onNext(newState) },
    mainFactory = { _, ctx -> master(ctx) },
    detailsFactory = ::details,
  )

  override val webHistoryNavigation: WebHistoryNavigation<*> = WebHistoryNavigation.ofPanels(
    navigator = nav,
    panels = panels,
    mainSerializer = Unit.serializer(),
    detailsSerializer = Configuration.serializer(),
    extraSerializer = NothingSerializer(),
    pathMapper = {
      if (it.details != null) {
        "details"
      } else ""
    },
  )

  override fun setMultiPane(isMultiPane: Boolean) =
    nav.setMultiPane(isMultiPane)

  override fun onNavigateBack() {
    nav.popElse(navigateBack)
  }

  private fun master(componentContext: UserComponentContext) =
    ProductListComponent(
      componentContext = componentContext,
      dependencies = object : ProductListComponent.Dependencies {
        override val input: Flow<ProductListComponent.Input> = listInput
        override val output: suspend (ProductListComponent.Output) -> Unit = { out ->
          when (out) {
            is ProductListComponent.Output.OnItemSelected ->
              nav.activateDetails(Configuration.View(out.item.id))

            is ProductListComponent.Output.OnCreateNew ->
              nav.activateDetails(Configuration.Create)
          }
        }
      },
    )

  private fun details(
    configuration: Configuration,
    componentContext: UserComponentContext,
  ): ProductDetailsComponent = ProductDetailsComponent(
    componentContext = componentContext,
    dependencies = object : ProductDetailsComponent.Dependencies {
      override val mode: Configuration = configuration
      override val output: suspend (ProductDetailsComponent.Output) -> Unit = { out ->
        when (out) {
          is ProductDetailsComponent.Output.OnItemCreated ->
            listInput.emit(ProductListComponent.Input.OnItemCreated(out.item))

          is ProductDetailsComponent.Output.OnItemUpdated ->
            listInput.emit(ProductListComponent.Input.OnItemUpdated(out.item))
        }
      }
    },
    navigateBack = { nav.popElse(navigateBack) },
  )

}