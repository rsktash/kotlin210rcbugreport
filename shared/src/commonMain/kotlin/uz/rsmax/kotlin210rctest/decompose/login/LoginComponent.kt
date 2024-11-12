package uz.rsmax.kotlin210rctest.decompose.login

import app.cash.sqldelight.async.coroutines.awaitAsOne
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
import uz.rsmax.kotlin210rctest.decompose.AppComponentContext
import uz.rsmax.kotlin210rctest.decompose.AppUser
import uz.rsmax.kotlin210rctest.decompose.BaseComponent
import uz.rsmax.kotlin210rctest.decompose.SideEffectComponent
import uz.rsmax.kotlin210rctest.decompose.asValue
import uz.rsmax.kotlin210rctest.decompose.database.userTableToData
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent.Model
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent.Output
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent.SideEffect
import uz.rsmax.kotlin210rctest.decompose.login.LoginStore.Intent
import uz.rsmax.kotlin210rctest.decompose.login.LoginStore.Label
import uz.rsmax.kotlin210rctest.decompose.login.LoginStore.State
import uz.rsmax.kotlin210rctest.decompose.prefs.updateLoggedInUser

fun LoginComponent(
  componentContext: AppComponentContext,
  dependencies: Dependencies,
): LoginComponent = LoginComponentImpl(componentContext, dependencies)


interface LoginComponent : BaseComponent<Model>, SideEffectComponent<SideEffect> {
  data class Model(
    val user: NewUser,
    val isDataValid: Boolean,
  )

  sealed interface Output {
    data class OnUserLoggedIn(val user: AppUser) : Output
  }

  sealed interface SideEffect {
    data class OnError(val error: Throwable) : SideEffect
  }

  interface Dependencies {
    val output: suspend (Output) -> Unit
  }

  fun onEditName(name: String)

  fun onEditEmail(email: String)

  fun onSingIn()
}

interface LoginStore : Store<Intent, State, Label> {

  data class State(
    val user: NewUser = NewUser("", "")
  )

  sealed interface Intent {
    data class OnEditName(val name: String) : Intent
    data class OnEditEmail(val email: String) : Intent
    data object OnSignIn : Intent
  }

  sealed interface Label {
    data class OnUserLoggedIn(val user: AppUser) : Label
    data class OnError(val error: Throwable) : Label
  }
}

internal class LoginComponentImpl(
  componentContext: AppComponentContext,
  dependencies: Dependencies
) : LoginComponent,
  AppComponentContext by componentContext,
  Dependencies by dependencies {

  private val store = instanceKeeper.getStore {
    object : LoginStore, Store<Intent, State, Label> by storeFactory.create(
      initialState = State(),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory {
        onIntent<Intent> { intent ->
          when (intent) {
            is Intent.OnEditName -> dispatch(Message.OnNameChanged(intent.name))
            is Intent.OnEditEmail -> dispatch(Message.OnEmailChanged(intent.email))
            is Intent.OnSignIn -> {
              val user = state().user
              if (user.isDataValid()) {
                launch {
                  try {
                    val newUser = database.appUserTableQueries.insert(
                      name = user.name,
                      email = user.email,
                    ).awaitAsOne()
                    preferences.updateLoggedInUser(newUser.id)
                    publish(Label.OnUserLoggedIn(newUser.let(userTableToData)))
                  } catch (err: Throwable) {
                    publish(Label.OnError(err))
                  }
                }
              }
            }
          }
        }
      },
      reducer = { msg: Message ->
        when (msg) {
          is Message.OnNameChanged -> copy(user = user.copy(name = msg.name))
          is Message.OnEmailChanged -> copy(user = user.copy(email = msg.email))
        }
      }
    ) {}
  }

  override val model: Value<Model> = store.asValue().map(stateToModel)

  override val sideEffect: Flow<SideEffect> = store.labels.mapNotNull(labelToSideEffect)

  init {
    bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
      store.labels.mapNotNull(labelToOutput) bindTo output
    }
  }

  override fun onEditName(name: String) = store.accept(Intent.OnEditName(name))
  override fun onEditEmail(email: String) = store.accept(Intent.OnEditEmail(email))
  override fun onSingIn() = store.accept(Intent.OnSignIn)

  private sealed interface Message {
    data class OnNameChanged(val name: String) : Message
    data class OnEmailChanged(val email: String) : Message
  }

  companion object {
    private val stateToModel: (State) -> Model = {
      Model(
        user = it.user,
        isDataValid = it.user.isDataValid(),
      )
    }
    private val labelToOutput: (Label) -> Output? = {
      when (it) {
        is Label.OnUserLoggedIn -> Output.OnUserLoggedIn(it.user)
        is Label.OnError -> null
      }
    }
    private val labelToSideEffect: (Label) -> SideEffect? = {
      when (it) {
        is Label.OnError -> SideEffect.OnError(it.error)
        is Label.OnUserLoggedIn -> null
      }
    }

    private fun NewUser.isDataValid(): Boolean = name.isNotEmpty() && email.isNotEmpty()
  }
}