package uz.rsmax.kotlin210rctest.decompose.app.product.details.view

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import uz.rsmax.kotlin210rctest.decompose.BaseNavBackComponent
import uz.rsmax.kotlin210rctest.decompose.LoadableData
import uz.rsmax.kotlin210rctest.decompose.LoadableData.Companion.isLoaded
import uz.rsmax.kotlin210rctest.decompose.NavigateBackState
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductId
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductItem
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent.Input
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent.Model
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewComponent.Output
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewStore.Intent
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewStore.Label
import uz.rsmax.kotlin210rctest.decompose.app.product.details.view.ProductViewStore.State
import uz.rsmax.kotlin210rctest.decompose.asValue
import uz.rsmax.kotlin210rctest.decompose.database.productTableToData

fun ProductViewComponent(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  navigateBack: () -> Unit,
): ProductViewComponent = ProductViewComponentImpl(componentContext, dependencies, navigateBack)

interface ProductViewComponent : BaseNavBackComponent<Model> {
  data class Model(
    val item: LoadableData<ProductItem>,
    override val canNavigateBack: Boolean,
  ) : NavigateBackState

  sealed interface Input {
    data class OnItemUpdated(val item: ProductItem) : Input
  }

  sealed interface Output {
    data class OnEditItem(val item: ProductItem) : Output
  }

  interface Dependencies {
    val productId: ProductId
    val input: Flow<Input>
    val output: suspend (Output) -> Unit
  }

  fun onEditItem()
}

interface ProductViewStore : Store<Intent, State, Label> {
  data class State(
    val item: LoadableData<ProductItem> = LoadableData.Idle,
    val canNavigateBack: Boolean = true,
  )

  sealed interface Intent {
    data object OnEditItem : Intent
    data class OnItemUpdated(val item: ProductItem) : Intent
  }

  sealed interface Label {
    data class OnEditItem(val item: ProductItem) : Label
  }
}

internal class ProductViewComponentImpl(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  private val navigateBack: () -> Unit
) : ProductViewComponent,
  UserComponentContext by componentContext,
  Dependencies by dependencies {

  private val store = instanceKeeper.getStore {
    object : ProductViewStore, Store<Intent, State, Label> by storeFactory.create(
      initialState = State(),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory {
        onAction<Unit> {
          launch {
            val product = database.productTableQueries.findById(productId).awaitAsOneOrNull()
            if (product != null) {
              dispatch(Message.OnInitialDataLoaded(product.let(productTableToData)))
            }
          }
        }
        onIntent<Intent> { intent ->
          when (intent) {
            is Intent.OnEditItem -> {
              val item = state().item
              if (item.isLoaded()) {
                publish(Label.OnEditItem(item.data))
              }
            }

            is Intent.OnItemUpdated -> {
              dispatch(Message.OnInitialDataLoaded(intent.item))
            }
          }
        }
      },
      reducer = { msg: Message ->
        when (msg) {
          is Message.OnInitialDataLoaded -> copy(item = LoadableData.Loaded(msg.product))
        }
      }
    ) {}
  }

  override val model: Value<Model> = store.asValue().map(stateToModel)

  init {
    bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
      input.mapNotNull(inputToIntent) bindTo store
      store.labels.mapNotNull(labelToOutput) bindTo output
    }
  }

  override fun onNavigateBack() {
    navigateBack()
  }

  override fun onEditItem() = store.accept(Intent.OnEditItem)

  private sealed interface Message {
    data class OnInitialDataLoaded(val product: ProductItem) : Message
  }

  companion object {
    private val stateToModel: (State) -> Model = { Model(it.item, it.canNavigateBack) }
    private val inputToIntent: (Input) -> Intent = {
      when (it) {
        is Input.OnItemUpdated -> Intent.OnItemUpdated(it.item)
      }
    }
    private val labelToOutput: (Label) -> Output = {
      when (it) {
        is Label.OnEditItem -> Output.OnEditItem(it.item)
      }
    }
  }

}