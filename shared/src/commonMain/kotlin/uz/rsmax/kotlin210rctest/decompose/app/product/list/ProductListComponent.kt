package uz.rsmax.kotlin210rctest.decompose.app.product.list

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import uz.rsmax.kotlin210rctest.decompose.BaseNavBackComponent
import uz.rsmax.kotlin210rctest.decompose.LoadableData
import uz.rsmax.kotlin210rctest.decompose.LoadableData.Companion.edit
import uz.rsmax.kotlin210rctest.decompose.NavigateBackState
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductItem
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent.Input
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent.Model
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListComponent.Output
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListStore.Intent
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListStore.Label
import uz.rsmax.kotlin210rctest.decompose.app.product.list.ProductListStore.State
import uz.rsmax.kotlin210rctest.decompose.asValue
import uz.rsmax.kotlin210rctest.decompose.database.productTableToData

fun ProductListComponent(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  navigateBack: (() -> Unit)? = null,
): ProductListComponent = ProductListComponentImpl(componentContext, dependencies, navigateBack)

interface ProductListComponent : BaseNavBackComponent<Model> {
  data class Model(
    val items: LoadableData<List<ProductItem>>,
    override val canNavigateBack: Boolean,
  ) : NavigateBackState

  sealed interface Output {
    data object OnCreateNew : Output
    data class OnItemSelected(val item: ProductItem) : Output
  }

  sealed interface Input {
    data class OnItemCreated(val item: ProductItem) : Input
    data class OnItemUpdated(val item: ProductItem) : Input
  }

  interface Dependencies {
    val input: Flow<Input>
    val output: suspend (Output) -> Unit
  }

  fun onClickNew()
  fun onSelectItem(item: ProductItem)
}

interface ProductListStore : Store<Intent, State, Label> {
  data class State(
    val items: LoadableData<List<ProductItem>> = LoadableData.Idle,
    val canNavigateBack: Boolean = true,
  )

  sealed interface Label {
    data object OnCreateNew : Label
    data class OnItemSelected(val item: ProductItem) : Label
  }

  sealed interface Intent {
    data object OnClickNew : Intent
    data class OnSelectItem(val item: ProductItem) : Intent
    data class OnItemCreated(val item: ProductItem) : Intent
    data class OnItemUpdated(val item: ProductItem) : Intent

  }
}

internal class ProductListComponentImpl(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  private val navigateBack: (() -> Unit)?
) : ProductListComponent,
  UserComponentContext by componentContext,
  Dependencies by dependencies {

  private val store = instanceKeeper.getStore {
    object : ProductListStore, Store<Intent, State, Label> by storeFactory.create(
      initialState = State(canNavigateBack = navigateBack != null),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory {
        onAction<Unit> {
          launch {
            Napier.d("ProductList 1")
            val products = database.productTableQueries.findAll().awaitAsList()
            Napier.d("ProductList 2: ${products.size}")
//            Napier.d("ProductList 3: id: ${products[0].id}, name: ${products[0].name}, description: ${products[0].description}, price: ${products[0].price}, isDeleted: ${products[0].isDeleted}, createdAt: ${products[0].createdAt}, modifiedAt: ${products[0].modifiedAt}")
            dispatch(Message.OnInitialDataLoaded(products.map(productTableToData)))
          }
        }
        onIntent<Intent> { intent ->
          when (intent) {
            is Intent.OnClickNew -> publish(Label.OnCreateNew)
            is Intent.OnSelectItem -> publish(Label.OnItemSelected(intent.item))
            is Intent.OnItemCreated -> dispatch(Message.OnItemCreated(intent.item))
            is Intent.OnItemUpdated -> dispatch(Message.OnItemUpdated(intent.item))
          }
        }
      },
      reducer = { msg: Message ->
        when (msg) {
          is Message.OnInitialDataLoaded -> copy(items = LoadableData.Loaded(msg.products))
          is Message.OnItemCreated -> copy(items = items.edit { this + msg.item })
          is Message.OnItemUpdated -> copy(items = items.edit { map { if (it.id == msg.item.id) msg.item else it } })
        }
      }
    ) {}
  }

  override val model: Value<Model> = store.asValue().map(stateToModel)

  init {
    bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
      store.labels.mapNotNull(labelToOutput) bindTo output
      input.mapNotNull(inputToIntent) bindTo store
    }
  }

  override fun onNavigateBack() {
    navigateBack?.invoke()
  }

  override fun onClickNew() = store.accept(Intent.OnClickNew)
  override fun onSelectItem(item: ProductItem) = store.accept(Intent.OnSelectItem(item))

  private sealed interface Message {
    data class OnInitialDataLoaded(val products: List<ProductItem>) : Message
    data class OnItemCreated(val item: ProductItem) : Message
    data class OnItemUpdated(val item: ProductItem) : Message
  }

  companion object {
    private val stateToModel: (State) -> Model = { Model(it.items, it.canNavigateBack) }
    private val labelToOutput: (Label) -> Output? = {
      when (it) {
        is Label.OnCreateNew -> Output.OnCreateNew
        is Label.OnItemSelected -> Output.OnItemSelected(it.item)
      }
    }
    private val inputToIntent: (Input) -> Intent = {
      when (it) {
        is Input.OnItemCreated -> Intent.OnItemCreated(it.item)
        is Input.OnItemUpdated -> Intent.OnItemUpdated(it.item)
      }
    }
  }

}