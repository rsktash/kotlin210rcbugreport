package uz.rsmax.kotlin210rctest.decompose

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.Store

@OptIn(ExperimentalDecomposeApi::class)
fun <DC : Any, EC : Any> PanelsNavigation<Unit, DC, EC>.setMultiPane(isMultiPane: Boolean) {
  setMode(if (isMultiPane) ChildPanelsMode.DUAL else ChildPanelsMode.SINGLE)
}

fun <C : Any> StackNavigator<C>.popElse(optionalBack: (() -> Unit)?) {
  pop { success ->
    if (!success) {
      optionalBack?.invoke()
    }
  }
}

@OptIn(ExperimentalDecomposeApi::class)
fun <DC : Any, EC : Any> PanelsNavigation<Unit, DC, EC>.popElse(optionalBack: (() -> Unit)?) {
  pop { newState, oldState -> if (newState == oldState) optionalBack?.invoke() }
}

fun <T : Any> Store<*, T, *>.asValue(): Value<T> =
  object : Value<T>() {
    override val value: T
      get() = state

    override fun subscribe(observer: (T) -> Unit): Cancellation {
      val disposable = states(com.arkivanov.mvikotlin.core.rx.observer(onNext = observer))
      return Cancellation { disposable.dispose() }
    }
  }