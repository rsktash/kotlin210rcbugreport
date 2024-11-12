package uz.rsmax.kotlin210rctest.decompose

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.coroutines.flow.Flow

interface BaseNavBackEvents {
  fun onNavigateBack()
}
interface BaseComponent<Model : Any> : BackHandlerOwner {
   val model: Value<Model>
}
interface SideEffectComponent<SideEffect : Any>  {
  val sideEffect: Flow<SideEffect>
}

interface NavigateBackState {
  val canNavigateBack: Boolean
}

interface BaseNavBackComponent<Model : NavigateBackState> :
  BaseComponent<Model>, BaseNavBackEvents

interface BaseRouterComponent<Child : Any> : BackHandlerOwner {
   val stack: Value<ChildStack<*, Child>>
}

interface BaseRouterNavBackComponent<Child : Any> :
  BaseRouterComponent<Child>, BaseNavBackEvents