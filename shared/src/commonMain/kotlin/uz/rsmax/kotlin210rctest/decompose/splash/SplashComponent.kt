package uz.rsmax.kotlin210rctest.decompose.splash

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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.rsmax.AppDatabase
import uz.rsmax.kotlin210rctest.decompose.AppComponentContext
import uz.rsmax.kotlin210rctest.decompose.AppDispatchers
import uz.rsmax.kotlin210rctest.decompose.AppUser
import uz.rsmax.kotlin210rctest.decompose.BaseComponent
import uz.rsmax.kotlin210rctest.decompose.asValue
import uz.rsmax.kotlin210rctest.decompose.database.userTableToData
import uz.rsmax.kotlin210rctest.decompose.prefs.AppPreferences
import uz.rsmax.kotlin210rctest.decompose.prefs.getLoggedInUser
import uz.rsmax.kotlin210rctest.decompose.splash.SplashComponent.Dependencies
import uz.rsmax.kotlin210rctest.decompose.splash.SplashComponent.Model
import uz.rsmax.kotlin210rctest.decompose.splash.SplashComponent.Output
import uz.rsmax.kotlin210rctest.decompose.splash.SplashStore.Label
import uz.rsmax.kotlin210rctest.decompose.splash.SplashStore.State

fun SplashComponent(
  componentContext: AppComponentContext,
  dependencies: Dependencies,
): SplashComponent = SplashComponentImpl(componentContext, dependencies)

interface SplashComponent : BaseComponent<Model> {
  data class Model(
    val loading: Boolean
  )

  sealed interface Output {
    data object OnUserNotFound : Output
    data class OnUserFound(val user: AppUser) : Output
  }

  interface Dependencies {
    val output: suspend (Output) -> Unit
  }
}

interface SplashStore : Store<Unit, State, Label> {
  data class State(
    val loading: Boolean = true,
  )

  sealed interface Label {
    data object OnUserNotFound : Label
    data class OnUserFound(val user: AppUser) : Label
  }
}

interface SplashRepo {
  suspend fun getLoggedInUser(): AppUser?
}

private class SplashRepoImpl(
  private val dispatchers: AppDispatchers,
  private val database: AppDatabase,
  private val preferences: AppPreferences,
) : SplashRepo {
  override suspend fun getLoggedInUser(): AppUser? =
    withContext(dispatchers.io) {
      preferences.getLoggedInUser()?.let { id ->
        database.appUserTableQueries.findById(id).awaitAsOneOrNull()?.let(userTableToData)
      }
    }
}

internal class SplashComponentImpl(
  componentContext: AppComponentContext,
  dependencies: Dependencies
) : SplashComponent,
  AppComponentContext by componentContext,
  Dependencies by dependencies {

  private val repo = SplashRepoImpl(dispatchers, database, preferences)

  private val store = instanceKeeper.getStore {
    object : SplashStore, Store<Unit, State, Label> by storeFactory.create(
      initialState = State(),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory {
        onAction<Unit> {
          launch {
            val user = repo.getLoggedInUser()
            if (user != null) {
              publish(Label.OnUserFound(user))
            } else {
              publish(Label.OnUserNotFound)
            }
          }
        }
      }
    ) {}
  }
  override val model: Value<Model> = store.asValue().map(stateToModel)

  init {
    bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
      store.labels.mapNotNull(labelToOutput) bindTo output
    }
  }

  companion object {
    private val stateToModel: (State) -> Model = {
      Model(it.loading)
    }
    private val labelToOutput: (Label) -> Output? = {
      when (it) {
        is Label.OnUserFound -> Output.OnUserFound(it.user)
        is Label.OnUserNotFound -> Output.OnUserNotFound
      }
    }
  }
}