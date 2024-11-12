package uz.rsmax.kotlin210rctest.decompose.app.dashboard

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import uz.rsmax.kotlin210rctest.decompose.BaseComponent
import uz.rsmax.kotlin210rctest.decompose.app.UserComponentContext
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardComponent.Model
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardStore.Intent
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardStore.Label
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardStore.State
import uz.rsmax.kotlin210rctest.decompose.asValue

fun DashboardComponent(
  componentContext: UserComponentContext
): DashboardComponent = DashboardComponentImpl(componentContext)

interface DashboardComponent : BaseComponent<Model> {
  data class Model(
    val notifications: List<String>
  )
}

interface DashboardStore : Store<Intent, State, Label> {
  data class State(
    val notifications: List<String> = emptyList()
  )

  sealed interface Intent {}
  sealed interface Label {}
}

internal class DashboardComponentImpl(
  componentContext: UserComponentContext,
) : DashboardComponent, UserComponentContext by componentContext {

  private val store = instanceKeeper.getStore {
    object : DashboardStore, Store<Intent, State, Label> by storeFactory.create(
      initialState = State(),
      bootstrapper = SimpleBootstrapper(Unit),
      executorFactory = coroutineExecutorFactory { }
    ) {}
  }
  override val model: Value<Model> = store.asValue().map { Model(it.notifications) }

}