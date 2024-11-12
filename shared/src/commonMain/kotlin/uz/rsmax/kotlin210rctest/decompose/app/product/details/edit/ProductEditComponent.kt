package uz.rsmax.kotlin210rctest.decompose.app.product.details.edit

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import uz.rsmax.base.EditableData
import uz.rsmax.base.EditableData.Companion.editData
import uz.rsmax.base.EditableData.Companion.isLoaded
import uz.rsmax.base.EditableData.Companion.isLoadedAnd
import uz.rsmax.kotlin210rctest.decompose.BaseNavBackComponent
import uz.rsmax.kotlin210rctest.decompose.NavigateBackState
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductId
import uz.rsmax.kotlin210rctest.decompose.app.product.ProductItem
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditComponent.Model
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditStore.Intent
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditStore.Label
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditStore.State
import uz.rsmax.kotlin210rctest.decompose.asValue
import uz.rsmax.kotlin210rctest.decompose.database.productTableToData
import uz.rsmax.kotlin210rctest.decompose.app.product.details.edit.ProductEditComponent.Output

fun ProductEditComponent(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  navigateBack: () -> Unit,
): ProductEditComponent = ProductEditComponentImpl(componentContext, dependencies, navigateBack)

interface ProductEditComponent : BaseNavBackComponent<Model> {
  data class Model(
    val item: EditableData<ProductItem>,
    val isDataValid: Boolean,
    override val canNavigateBack: Boolean,
  ) : NavigateBackState

  sealed interface Output {
    data class OnItemUpdated(val item: ProductItem) : Output
    data class OnItemCreated(val item: ProductItem) : Output
  }

  interface Dependencies {
    val itemId: ProductId?
    val output: suspend (Output) -> Unit
  }

  fun onEditName(name: String)
  fun onEditDescription(description: String)
  fun onEditPrice(price: Double)
  fun onSave()
  fun onDisposeChanges()
}

interface ProductEditStore : Store<Intent, State, Label> {
  data class State(
    val item: EditableData<ProductItem> = EditableData.Idle,
    val canNavigateBack: Boolean = true,
    val promptDisposeChanges: Boolean = false,
  )

  sealed interface Label {
    data class OnItemUpdated(val item: ProductItem) : Label
    data class OnItemCreated(val item: ProductItem) : Label
    data object OnNavigateBack : Label
  }

  sealed interface Intent {
    data object OnNavigateBack : Intent
    data class OnEditName(val name: String) : Intent
    data class OnEditDescription(val description: String) : Intent
    data class OnEditPrice(val price: Double) : Intent
    data object OnSave : Intent
    data object OnDisposeChanges : Intent
  }
}

internal class ProductEditComponentImpl(
  componentContext: UserComponentContext,
  dependencies: Dependencies,
  private val navigateBack: () -> Unit,
) : ProductEditComponent,
  UserComponentContext by componentContext,
  Dependencies by dependencies {
  private val store = instanceKeeper.getStore {
    object : ProductEditStore, Store<Intent, State, Label> by storeFactory.create(
      initialState = State(),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory {
        onAction<Unit> {
          if (itemId != null) {
            launch {
              val product = database.productTableQueries.findById(itemId).awaitAsOneOrNull()
              if (product != null) {
                dispatch(Message.OnInitialDataLoaded(product.let(productTableToData)))
              }
            }
          } else {
            dispatch(Message.OnInitialDataLoaded(ProductItem.createNew()))
          }
        }
        onIntent<Intent> { intent ->
          when (intent) {
            is Intent.OnEditName ->
              dispatch(Message.OnNameChanged(intent.name))

            is Intent.OnEditDescription ->
              dispatch(Message.OnDescriptionChanged(intent.description))

            is Intent.OnEditPrice ->
              dispatch(Message.OnPriceChanged(intent.price))

            is Intent.OnNavigateBack -> {
              with(state()) {
                if (item.loadedAndIsDirty) {
                  dispatch(Message.OnSwitchPromptDisposeChanges)
                } else {
                  publish(Label.OnNavigateBack)
                }
              }
            }

            is Intent.OnDisposeChanges ->
              publish(Label.OnNavigateBack)

            is Intent.OnSave -> {
              with(state()) {
                if (item.isLoaded() && item.data.isValid()) {
                  launch {
                    val product = item.data
                    val isNew = product.id == ProductId.NewId
                    val savedProduct = if (isNew) {
                      database.productTableQueries.insert(
                        name = product.name,
                        description = product.description,
                        price = product.price,
                        isDeleted = false,
                        createdAt = Clock.System.now(),
                        modifiedAt = Clock.System.now(),
                      ).awaitAsOne()
                    } else {
                      database.productTableQueries.update(
                        name = product.name,
                        description = product.description,
                        price = product.price,
                        isDeleted = product.isDeleted,
                        id = product.id,
                        modifiedAt = Clock.System.now(),
                      ).awaitAsOne()
                    }.let(productTableToData)

                    if (isNew) publish(Label.OnItemCreated(savedProduct))
                    else publish(Label.OnItemUpdated(savedProduct))

                    dispatch(Message.OnInitialDataLoaded(savedProduct))
                  }
                }
              }
            }
          }
        }
      },
      reducer = { msg: Message ->
        when (msg) {
          is Message.OnInitialDataLoaded -> copy(item = EditableData.Loaded(msg.item))
          is Message.OnNameChanged -> copy(item = item.editData { copy(name = msg.name) })
          is Message.OnDescriptionChanged -> copy(item = item.editData { copy(description = msg.description) })
          is Message.OnPriceChanged -> copy(item = item.editData { copy(price = msg.price) })
          is Message.OnSwitchPromptDisposeChanges -> copy(promptDisposeChanges = !promptDisposeChanges)
        }
      }
    ) {}
  }

  override val model: Value<Model> = store.asValue().map(stateToModel)

  private val backCallback = BackCallback(onBack = ::onNavigateBack)

  init {
    bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
      store.labels.mapNotNull(labelToNavigateBack) bindTo { navigateBack.invoke() }
      store.labels.mapNotNull(labelToOutput) bindTo output
    }
    model.subscribe(lifecycle) { s ->
      backCallback.isEnabled = s.item.loadedAndIsDirty
    }
  }

  override fun onNavigateBack() =
    store.accept(Intent.OnNavigateBack)

  override fun onEditName(name: String) =
    store.accept(Intent.OnEditName(name))

  override fun onEditDescription(description: String) =
    store.accept(Intent.OnEditDescription(description))

  override fun onEditPrice(price: Double) =
    store.accept(Intent.OnEditPrice(price))

  override fun onSave() =
    store.accept(Intent.OnSave)

  override fun onDisposeChanges() =
    store.accept(Intent.OnDisposeChanges)

  private sealed interface Message {
    data class OnInitialDataLoaded(val item: ProductItem) : Message
    data class OnNameChanged(val name: String) : Message
    data class OnDescriptionChanged(val description: String) : Message
    data class OnPriceChanged(val price: Double) : Message
    data object OnSwitchPromptDisposeChanges : Message
  }

  companion object {
    private val stateToModel: (State) -> Model =
      {
        Model(
          item = it.item,
          isDataValid = it.item.isLoadedAnd { data.isValid() },
          canNavigateBack = it.canNavigateBack,
        )
      }
    private val labelToOutput: (Label) -> Output? = {
      when(it){
        is Label.OnItemCreated -> Output.OnItemCreated(it.item)
        is Label.OnItemUpdated -> Output.OnItemUpdated(it.item)
        is Label.OnNavigateBack -> null
      }
    }
    private val labelToNavigateBack: (Label) -> Unit? = {
      when (it) {
        is Label.OnNavigateBack -> Unit
        else -> null
      }
    }

    private fun ProductItem.isValid(): Boolean =
      name.isNotEmpty() && description.isNotEmpty() && price > 0.0
  }

}