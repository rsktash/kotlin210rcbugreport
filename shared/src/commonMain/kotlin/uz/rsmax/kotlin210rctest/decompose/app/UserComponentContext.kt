package uz.rsmax.kotlin210rctest.decompose.app

import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import uz.rsmax.kotlin210rctest.decompose.AppComponentContext
import uz.rsmax.kotlin210rctest.decompose.AppComponentContextOwner
import uz.rsmax.kotlin210rctest.decompose.AppUser

interface UserComponentContextOwner : AppComponentContextOwner {
  val user: AppUser
}

interface UserComponentContext : GenericComponentContext<UserComponentContext>,
  UserComponentContextOwner

class DefaultUserComponentContext(
  componentContext: AppComponentContext,
  override val user: AppUser,
) : UserComponentContext,
  AppComponentContextOwner by componentContext,
  LifecycleOwner by componentContext,
  StateKeeperOwner by componentContext,
  InstanceKeeperOwner by componentContext,
  BackHandlerOwner by componentContext {
  override val componentContextFactory: ComponentContextFactory<UserComponentContext> =
    ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
      val ctx = componentContext.componentContextFactory(
        lifecycle,
        stateKeeper,
        instanceKeeper,
        backHandler
      )
      DefaultUserComponentContext(ctx, user)
    }
}