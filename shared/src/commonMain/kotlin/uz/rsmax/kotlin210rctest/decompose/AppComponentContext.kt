package uz.rsmax.kotlin210rctest.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.arkivanov.mvikotlin.core.store.StoreFactory
import uz.rsmax.AppDatabase
import uz.rsmax.kotlin210rctest.decompose.database.DatabaseMigrator
import uz.rsmax.kotlin210rctest.decompose.prefs.AppPreferences

interface AppComponentContextOwner {
  val dispatchers: AppDispatchers
  val database: AppDatabase
  val migrator: DatabaseMigrator
  val preferences: AppPreferences
  val storeFactory: StoreFactory
}

interface AppComponentContext : GenericComponentContext<AppComponentContext>,
  AppComponentContextOwner

expect fun DefaultAppComponentContext(
  componentContext: ComponentContext,
  storeFactory: StoreFactory,
): DefaultAppComponentContext

class DefaultAppComponentContext(
  componentContext: ComponentContext,
  override val dispatchers: AppDispatchers,
  override val database: AppDatabase,
  override val migrator: DatabaseMigrator,
  override val preferences: AppPreferences,
  override val storeFactory: StoreFactory,
) : AppComponentContext,
  LifecycleOwner by componentContext,
  StateKeeperOwner by componentContext,
  InstanceKeeperOwner by componentContext,
  BackHandlerOwner by componentContext {
  override val componentContextFactory: ComponentContextFactory<DefaultAppComponentContext> =
    ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
      val ctx = componentContext.componentContextFactory(
        lifecycle,
        stateKeeper,
        instanceKeeper,
        backHandler
      )
      DefaultAppComponentContext(ctx, dispatchers, database, migrator, preferences, storeFactory)
    }
}
