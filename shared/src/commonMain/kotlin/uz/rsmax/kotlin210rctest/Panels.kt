package uz.rsmax.kotlin210rctest

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsAnimators
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsLayout
import com.arkivanov.decompose.extensions.compose.experimental.panels.HorizontalChildPanelsLayout
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.LocalStackAnimationProvider
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.Direction
import com.arkivanov.decompose.extensions.compose.stack.animation.isExit
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.keyHashString
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.ChildPanelsMode.DUAL
import com.arkivanov.decompose.router.panels.ChildPanelsMode.SINGLE
import com.arkivanov.decompose.router.panels.ChildPanelsMode.TRIPLE
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Displays the provided [ChildPanels], taking care of saving and restoring the UI state. This
 * variant supports up to two child components: Main (required) and Details (optional).
 *
 * Child Panels layout is comparable with Compose
 * [List-Details Layout](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail).
 *
 * @param panels an observable [ChildPanels] to be displayed.
 * @param mainChild a `Composable` function that displays the provided Main component.
 * @param detailsChild a `Composable` function that displays the provided Details component.
 * @param modifier a [Modifier] to applied to a wrapping container.
 * @param layout an implementation of [ChildPanelsLayout] responsible for laying out panels. The
 *   default layout is [HorizontalChildPanelsLayout].
 * @param animators a [ChildPanelsAnimators] containing panel animators for different kinds of
 *   layouts.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildPanels], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is not `null`, and disabled if the returned value is `null`. Only works
 *   if [ChildPanels.mode] is [ChildPanelsMode.SINGLE].
 */
@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any> ChildPanels(
    panels: Value<ChildPanels<MC, MT, DC, DT, *, *>>,
    mainChild: @Composable (Child.Created<MC, MT>) -> Unit,
    detailsChild: @Composable (Child.Created<DC, DT>) -> Unit,
    modifier: Modifier = Modifier,
    layout: ChildPanelsLayout = remember { HorizontalChildPanelsLayout() },
    animators: ChildPanelsAnimators = remember { ChildPanelsAnimators() },
    predictiveBackParams: (ChildPanels<MC, MT, DC, DT, *, *>) -> PredictiveBackParams? = { null },
) {
  ChildPanels(
      panels = panels,
      mainChild = mainChild,
      detailsChild = detailsChild,
      extraChild = {},
      modifier = modifier,
      layout = layout,
      animators = animators,
      predictiveBackParams = predictiveBackParams,
  )
}

/**
 * Displays the provided [ChildPanels], taking care of saving and restoring the UI state. This
 * variant supports up to two child components: Main (required) and Details (optional).
 *
 * Child Panels layout is comparable with Compose
 * [List-Details Layout](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail).
 *
 * @param panels a [ChildPanels] to be displayed.
 * @param mainChild a `Composable` function that displays the provided Main component.
 * @param detailsChild a `Composable` function that displays the provided Details component.
 * @param modifier a [Modifier] to applied to a wrapping container.
 * @param layout an implementation of [ChildPanelsLayout] responsible for laying out panels.
 * @param animators a [ChildPanelsAnimators] containing panel animators for different kinds of
 *   layouts.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildPanels], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is not `null`, and disabled if the returned value is `null`. Only works
 *   if [ChildPanels.mode] is [ChildPanelsMode.SINGLE].
 */
@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any> ChildPanels(
    panels: ChildPanels<MC, MT, DC, DT, *, *>,
    mainChild: @Composable (Child.Created<MC, MT>) -> Unit,
    detailsChild: @Composable (Child.Created<DC, DT>) -> Unit,
    modifier: Modifier = Modifier,
    layout: ChildPanelsLayout = remember { HorizontalChildPanelsLayout() },
    animators: ChildPanelsAnimators = remember { ChildPanelsAnimators() },
    predictiveBackParams: (ChildPanels<MC, MT, DC, DT, *, *>) -> PredictiveBackParams? = { null },
) {
  ChildPanels(
      panels = panels,
      mainChild = mainChild,
      detailsChild = detailsChild,
      extraChild = {},
      modifier = modifier,
      layout = layout,
      animators = animators,
      predictiveBackParams = predictiveBackParams,
  )
}

/**
 * Displays the provided [ChildPanels], taking care of saving and restoring the UI state. This
 * variant supports up to three child components: Main (required), Details (optional) and Extra
 * (optional).
 *
 * Child Panels layout is comparable with Compose
 * [List-Details Layout](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail).
 *
 * @param panels an observable [ChildPanels] to be displayed.
 * @param mainChild a `Composable` function that displays the provided Main component.
 * @param detailsChild a `Composable` function that displays the provided Details component.
 * @param extraChild a `Composable` function that displays the provided Extra component.
 * @param modifier a [Modifier] to be applied to a wrapping container.
 * @param layout an implementation of [ChildPanelsLayout] responsible for laying out panels.
 * @param animators a [ChildPanelsAnimators] containing panel animators for different kinds of
 *   layouts.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildPanels], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is not `null`, and disabled if the returned value is `null`. Only works
 *   if [ChildPanels.mode] is [ChildPanelsMode.SINGLE].
 */
@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any, EC : Any, ET : Any> ChildPanels(
    panels: Value<ChildPanels<MC, MT, DC, DT, EC, ET>>,
    mainChild: @Composable (Child.Created<MC, MT>) -> Unit,
    detailsChild: @Composable (Child.Created<DC, DT>) -> Unit,
    extraChild: @Composable (Child.Created<EC, ET>) -> Unit,
    modifier: Modifier = Modifier,
    layout: ChildPanelsLayout = remember { HorizontalChildPanelsLayout() },
    animators: ChildPanelsAnimators = remember { ChildPanelsAnimators() },
    predictiveBackParams: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> PredictiveBackParams? = { null },
) {
  val state = panels.subscribeAsState()

  ChildPanels(
      panels = state.value,
      mainChild = mainChild,
      detailsChild = detailsChild,
      extraChild = extraChild,
      modifier = modifier,
      layout = layout,
      animators = animators,
      predictiveBackParams = predictiveBackParams,
  )
}

/**
 * Displays the provided [ChildPanels], taking care of saving and restoring the UI state. This
 * variant supports up to three child components: Main (required), Details (optional) and Extra
 * (optional).
 *
 * Child Panels layout is comparable with Compose
 * [List-Details Layout](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail).
 *
 * @param panels a [ChildPanels] to be displayed.
 * @param mainChild a `Composable` function that displays the provided Main component.
 * @param detailsChild a `Composable` function that displays the provided Details component.
 * @param extraChild a `Composable` function that displays the provided Extra component.
 * @param modifier a [Modifier] to applied to a wrapping container.
 * @param layout an implementation of [ChildPanelsLayout] responsible for laying out panels.
 * @param animators a [ChildPanelsAnimators] containing panel animators for different kinds of
 *   layouts.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildPanels], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is not `null`, and disabled if the returned value is `null`. Only works
 *   if [ChildPanels.mode] is [ChildPanelsMode.SINGLE].
 */
@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any, EC : Any, ET : Any> ChildPanels(
    panels: ChildPanels<MC, MT, DC, DT, EC, ET>,
    mainChild: @Composable (Child.Created<MC, MT>) -> Unit,
    detailsChild: @Composable (Child.Created<DC, DT>) -> Unit,
    extraChild: @Composable (Child.Created<EC, ET>) -> Unit,
    modifier: Modifier = Modifier,
    layout: ChildPanelsLayout = remember { HorizontalChildPanelsLayout() },
    animators: ChildPanelsAnimators = remember { ChildPanelsAnimators() },
    predictiveBackParams: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> PredictiveBackParams? = { null },
) {
  val main = remember(panels.main) { panels.main.asPanelChild() }
  val details = remember(panels.details) { panels.details?.asPanelChild() }
  val extra = remember(panels.extra) { panels.extra?.asPanelChild() }
  val mode = panels.mode
  val broadcastPredictiveBackParams =
      rememberBroadcastPredictiveBackParams(key = panels, count = 2) {
        predictiveBackParams(panels)
      }

  Box(modifier = modifier) {
    layout.Layout(
        mode = mode,
        main = {
          MainPanel(
              main = main,
              mode = mode,
              hasDetails = details != null,
              hasExtra = extra != null,
              animators = animators,
              predictiveBackParams = broadcastPredictiveBackParams,
              content = mainChild,
          )
        },
        details = {
          DetailsPanel(
              details = details,
              mode = mode,
              hasExtra = extra != null,
              animators = animators,
              predictiveBackParams = broadcastPredictiveBackParams,
              content = detailsChild,
          )
        },
        extra = {
          ExtraPanel(
              extra = extra,
              mode = mode,
              animators = animators,
              predictiveBackParams = broadcastPredictiveBackParams,
              content = extraChild,
          )
        },
    )
  }
}

@ExperimentalDecomposeApi
@Composable
private fun <MC : Any, MT : Any> MainPanel(
    main: Child.Created<MC, PanelChild.Panel<MC, MT>>,
    mode: ChildPanelsMode,
    hasDetails: Boolean,
    hasExtra: Boolean,
    animators: ChildPanelsAnimators,
    predictiveBackParams: Lazy<PredictiveBackParams?>,
    content: @Composable (Child.Created<MC, MT>) -> Unit,
) {
  ChildStack(
      stack =
          when (mode) {
            SINGLE ->
                stackOfNotNull(
                    main, EmptyChild1.takeIf { hasDetails }, EmptyChild2.takeIf { hasExtra })
            DUAL,
            TRIPLE -> stackOfNotNull(main)
          },
      modifier = Modifier.fillMaxSize(),
      animation =
          stackAnimation(
              animator =
                  when (mode) {
                    SINGLE -> animators.single
                    DUAL -> animators.dual.first
                    TRIPLE -> animators.triple.first
                  },
              predictiveBackParams = {
                if (it.active != main) predictiveBackParams.value else null
              },
          ),
  ) {
    when (val child = it.instance) {
      is PanelChild.Panel -> content(child.child)
      is PanelChild.Empty -> Unit // no-op
    }
  }
}

@ExperimentalDecomposeApi
@Composable
private fun <DC : Any, DT : Any> DetailsPanel(
    details: Child.Created<DC, PanelChild.Panel<DC, DT>>?,
    mode: ChildPanelsMode,
    hasExtra: Boolean,
    animators: ChildPanelsAnimators,
    predictiveBackParams: Lazy<PredictiveBackParams?>,
    content: @Composable (Child.Created<DC, DT>) -> Unit,
) {
  ChildStack(
      stack =
          when (mode) {
            SINGLE ->
                stackOfNotNull(
                    EmptyChild1, details, EmptyChild2.takeIf { (details != null) && hasExtra })
            DUAL ->
                stackOfNotNull(
                    EmptyChild3, details, EmptyChild4.takeIf { (details != null) && hasExtra })
            TRIPLE -> stackOfNotNull(EmptyChild3, details)
          },
      modifier = Modifier.fillMaxSize(),
      animation =
          stackAnimation(
              animator =
                  when (mode) {
                    SINGLE -> animators.single
                    DUAL -> animators.dual.second
                    TRIPLE -> animators.triple.second
                  },
              predictiveBackParams = { stack ->
                when {
                  stack.active == EmptyChild2 -> predictiveBackParams.value
                  (stack.active == details) && (stack.items.first() == EmptyChild1) ->
                      predictiveBackParams.value
                  else -> null
                }
              },
          ),
  ) {
    when (val child = it.instance) {
      is PanelChild.Panel -> content(child.child)
      is PanelChild.Empty -> Unit // no-op
    }
  }
}

@ExperimentalDecomposeApi
@Composable
private fun <EC : Any, ET : Any> ExtraPanel(
    extra: Child.Created<EC, PanelChild.Panel<EC, ET>>?,
    mode: ChildPanelsMode,
    animators: ChildPanelsAnimators,
    predictiveBackParams: Lazy<PredictiveBackParams?>,
    content: @Composable (Child.Created<EC, ET>) -> Unit,
) {
  ChildStack(
      stack = stackOfNotNull(if (mode == SINGLE) EmptyChild1 else EmptyChild2, extra),
      modifier = Modifier.fillMaxSize(),
      animation =
          stackAnimation(
              animator =
                  when (mode) {
                    SINGLE -> animators.single
                    DUAL -> animators.dual.second
                    TRIPLE -> animators.triple.third
                  },
              predictiveBackParams = {
                if (it.backStack.first() == EmptyChild1) predictiveBackParams.value else null
              },
          ),
  ) {
    when (val child = it.instance) {
      is PanelChild.Panel -> content(child.child)
      is PanelChild.Empty -> Unit // no-op
    }
  }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun rememberBroadcastPredictiveBackParams(
    key: Any,
    count: Int,
    params: () -> PredictiveBackParams?
): Lazy<PredictiveBackParams?> =
    rememberLazy(key) {
      params()?.run {
        var onBackCallCount = 0

        copy(
            backHandler = BroadcastBackHandler(backHandler),
            onBack = {
              if (++onBackCallCount == count) {
                onBackCallCount = 0
                onBack()
              }
            },
        )
      }
    }

private fun <C : Any, T : Any> stackOfNotNull(
    vararg stack: Child.Created<C, T>?
): ChildStack<C, T> =
    stack.filterNotNull().let { ChildStack(active = it.last(), backStack = it.dropLast(1)) }

private fun <C : Any, T : Any> Child.Created<C, T>.asPanelChild():
    Child.Created<C, PanelChild.Panel<C, T>> =
    Child.Created(configuration = configuration, PanelChild.Panel(child = this))

private val EmptyChild1 =
    Child.Created(configuration = EmptyConfig(value = 1), instance = PanelChild.Empty)
private val EmptyChild2 =
    Child.Created(configuration = EmptyConfig(value = 2), instance = PanelChild.Empty)
private val EmptyChild3 =
    Child.Created(configuration = EmptyConfig(value = 3), instance = PanelChild.Empty)
private val EmptyChild4 =
    Child.Created(configuration = EmptyConfig(value = 4), instance = PanelChild.Empty)

private data class EmptyConfig(val value: Any)

private sealed interface PanelChild<out C : Any, out T : Any> {
  class Panel<out C : Any, out T : Any>(val child: Child.Created<C, T>) : PanelChild<C, T>

  data object Empty : PanelChild<Nothing, Nothing>
}

@Composable
internal fun <T> rememberLazy(key: Any, provider: () -> T): Lazy<T> =
    remember(key) { lazy(provider) }

@OptIn(ExperimentalDecomposeApi::class)
class PredictiveBackParams(
    val backHandler: BackHandler,
    val onBack: () -> Unit,
    val animatable: (initialBackEvent: BackEvent) -> PredictiveBackAnimatable? = { null },
) {

  internal fun copy(
      backHandler: BackHandler = this.backHandler,
      onBack: () -> Unit = this.onBack,
      animatable: (initialBackEvent: BackEvent) -> PredictiveBackAnimatable? = this.animatable,
  ): PredictiveBackParams =
      PredictiveBackParams(
          backHandler = backHandler,
          onBack = onBack,
          animatable = animatable,
      )
}

internal class BroadcastBackHandler(
    private val delegate: BackHandler,
) : BackCallback(), BackHandler {
  private var callbacks = emptyList<BackCallback>()

  override fun onBackStarted(backEvent: BackEvent) {
    callbacks.forEach { it.onBackStarted(backEvent) }
  }

  override fun onBackProgressed(backEvent: BackEvent) {
    callbacks.forEach { it.onBackProgressed(backEvent) }
  }

  override fun onBackCancelled() {
    callbacks.forEach(BackCallback::onBackCancelled)
  }

  override fun onBack() {
    callbacks.forEach(BackCallback::onBack)
  }

  override fun isRegistered(callback: BackCallback): Boolean = callback in callbacks

  override fun register(callback: BackCallback) {
    check(callback !in callbacks)
    callbacks += callback
    callbacks = callbacks.sortedByDescending(BackCallback::priority)
    priority = callback.priority

    if (callbacks.size == 1) {
      delegate.register(this)
    }
  }

  override fun unregister(callback: BackCallback) {
    check(callback in callbacks)
    callbacks -= callback

    if (callbacks.isEmpty()) {
      delegate.unregister(this)
    }
  }
}

/**
 * Displays the provided [stack] of child components, taking care of saving and restoring the UI
 * state.
 *
 * @param stack a [ChildStack] to be displayed.
 * @param modifier a [Modifier] to applied to a wrapping container.
 * @param animation an optional [StackAnimation] for animating stack changes. If not provided (or
 *   `null`), then a default [StackAnimation] is obtained from [LocalStackAnimationProvider]. If
 *   that is also `null`, then there is no animation.
 * @param content a `Composable` function that displays the provided [Child][Child.Created]
 *   component. The receiver [AnimatedVisibilityScope] can be used for additional animations, such
 *   as
 *   [Shared Element Transitions](https://developer.android.com/develop/ui/compose/animation/shared-elements).
 */
@OptIn(InternalDecomposeApi::class)
@ExperimentalDecomposeApi
@Composable
fun <C : Any, T : Any> ChildStack(
    stack: ChildStack<C, T>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>? = null,
    content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
) {
  val holder = rememberSaveableStateHolder()

  holder.retainStates(stack.getKeys())

  val animationProvider = LocalStackAnimationProvider.current
  val anim =
      animation ?: remember(animationProvider, animationProvider::provide) ?: emptyStackAnimation()

  anim(stack = stack, modifier = modifier) { child ->
    holder.SaveableStateProvider(child.keyHashString()) { content(child) }
  }
}

/**
 * Displays the provided [stack] of child components, taking care of saving and restoring the UI
 * state.
 *
 * @param stack an observable [ChildStack] to be displayed.
 * @param modifier a [Modifier] to applied to a wrapping container.
 * @param animation an optional [StackAnimation] for animating stack changes. If not provided (or
 *   `null`), then a default [StackAnimation] is obtained from [LocalStackAnimationProvider]. If
 *   that is also `null`, then there is no animation.
 * @param content a `Composable` function that displays the provided [Child][Child.Created]
 *   component. The receiver [AnimatedVisibilityScope] can be used for additional animations, such
 *   as
 *   [Shared Element Transitions](https://developer.android.com/develop/ui/compose/animation/shared-elements).
 */
@ExperimentalDecomposeApi
@Composable
fun <C : Any, T : Any> ChildStack(
    stack: Value<ChildStack<C, T>>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>? = null,
    content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
) {
  val state = stack.subscribeAsState()

  ChildStack(stack = state.value, modifier = modifier, animation = animation, content = content)
}

@OptIn(InternalDecomposeApi::class)
private fun ChildStack<*, *>.getKeys(): Set<String> =
    items.mapTo(HashSet(), Child<*, *>::keyHashString)

@Composable
private fun SaveableStateHolder.retainStates(currentKeys: Set<String>) {
  val keys = remember(this) { Keys(currentKeys) }

  DisposableEffect(this, currentKeys) {
    keys.set.forEach {
      if (it !in currentKeys) {
        removeState(it)
      }
    }

    keys.set = currentKeys

    onDispose {}
  }
}

internal class Keys(var set: Set<Any>)

/**
 * Creates an implementation of [StackAnimation] that allows different [StackAnimator]s.
 *
 * @param disableInputDuringAnimation disables input and touch events while animating, default value
 *   is `true`.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildStack], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is *not* `null`, and disabled if the returned value is `null`.
 * @param selector provides an optional [StackAnimator] for the current [Child], other [Child] and
 *   [Direction].
 */
@ExperimentalDecomposeApi
fun <C : Any, T : Any> stackAnimation(
    disableInputDuringAnimation: Boolean = true,
    predictiveBackParams: (ChildStack<C, T>) -> PredictiveBackParams? = { null },
    selector:
        (
            child: Child.Created<C, T>,
            otherChild: Child.Created<C, T>,
            direction: Direction) -> StackAnimator?,
): StackAnimation<C, T> =
    DefaultStackAnimation(
        disableInputDuringAnimation = disableInputDuringAnimation,
        predictiveBackParams = predictiveBackParams,
        selector = selector,
    )

/**
 * Creates an implementation of [StackAnimation] with the provided [StackAnimator].
 *
 * @param animator an optional [StackAnimator] to be used for animation, default is [fade].
 * @param disableInputDuringAnimation disables input and touch events while animating, default value
 *   is `true`.
 * @param predictiveBackParams a function that returns [PredictiveBackParams] for the specified
 *   [ChildStack], or `null`. The predictive back gesture is enabled if the value returned for the
 *   specified [ChildStack] is not `null`, and disabled if the returned value is `null`.
 */
@ExperimentalDecomposeApi
fun <C : Any, T : Any> stackAnimation(
    animator: StackAnimator? = fade(),
    disableInputDuringAnimation: Boolean = true,
    predictiveBackParams: (ChildStack<C, T>) -> PredictiveBackParams? = { null },
): StackAnimation<C, T> =
    DefaultStackAnimation(
        disableInputDuringAnimation = disableInputDuringAnimation,
        predictiveBackParams = predictiveBackParams,
        selector = { _, _, _ -> animator },
    )

@ExperimentalDecomposeApi
internal fun <C : Any, T : Any> emptyStackAnimation(): StackAnimation<C, T> = EmptyStackAnimation()

private class EmptyStackAnimation<C : Any, T : Any> : StackAnimation<C, T> {

  @Composable
  override fun invoke(
      stack: ChildStack<C, T>,
      modifier: Modifier,
      content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
  ) {
    val transitionState = remember { MutableTransitionState(EnterExitState.Visible) }
    val transition = rememberTransition(transitionState)

    WithAnimatedVisibilityScope(transition) { Box(modifier = modifier) { content(stack.active) } }
  }
}

@Composable
internal fun WithAnimatedVisibilityScope(
    transition: Transition<EnterExitState>,
    block: @Composable AnimatedVisibilityScope.() -> Unit,
) {
  val scope = remember(transition) { SimpleAnimatedVisibilityScope(transition) }
  scope.block()
}

private class SimpleAnimatedVisibilityScope(
    override val transition: Transition<EnterExitState>,
) : AnimatedVisibilityScope

@ExperimentalDecomposeApi
internal class DefaultStackAnimation<C : Any, T : Any>(
    private val disableInputDuringAnimation: Boolean,
    private val predictiveBackParams: (ChildStack<C, T>) -> PredictiveBackParams?,
    private val selector:
        (
            child: Child.Created<C, T>,
            otherChild: Child.Created<C, T>,
            direction: Direction) -> StackAnimator?,
) : StackAnimation<C, T> {

  @Composable
  override operator fun invoke(
      stack: ChildStack<C, T>,
      modifier: Modifier,
      content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
  ) {
    var currentStack by remember { mutableStateOf(stack) }
    var items by remember { mutableStateOf(getAnimationItems(newStack = currentStack)) }
    var nextItems: Map<Any, AnimationItem<C, T>>? by remember { mutableStateOf(null) }
    val stackKeys = remember(stack) { stack.items.map { it.key } }
    val currentStackKeys = remember(currentStack) { currentStack.items.map { it.key } }

    if (stack != currentStack) {
      val oldStack = currentStack
      currentStack = stack

      val updateItems =
          when {
            stack.active.key == oldStack.active.key ->
                (items.keys.singleOrNull() != stack.active.key) ||
                    (items.values.singleOrNull()?.child?.instance != stack.active.instance)

            items.size == 1 -> items.keys.single() != stack.active.key
            else -> items.keys.toList() != stackKeys
          }

      if (updateItems) {
        val newItems = getAnimationItems(newStack = currentStack, oldStack = oldStack)
        if (items.size == 1) {
          items = newItems
        } else {
          nextItems = newItems
        }
      }
    }

    Box(modifier = modifier) {
      items.forEach { (key, item) ->
        key(key) {
          Child(
              item = item,
              onFinished = {
                if (item.direction.isExit) {
                  items -= key
                } else {
                  items += (key to item.copy(animator = null))
                }
              },
              content = content,
          )

          if (item.direction.isExit) {
            DisposableEffect(Unit) {
              onDispose {
                nextItems?.also { items = it }
                nextItems = null
              }
            }
          }
        }
      }

      // A workaround until https://issuetracker.google.com/issues/214231672.
      // Normally only the exiting child should be disabled.
      if (disableInputDuringAnimation && ((items.size > 1) || (nextItems != null))) {
        Overlay(modifier = Modifier.matchParentSize())
      }
    }

    if (currentStack.backStack.isNotEmpty()) {
      val predictiveBackParams = remember(currentStackKeys) { predictiveBackParams(currentStack) }
      if (predictiveBackParams != null) {
        key(currentStackKeys) {
          PredictiveBackController(
              stack = currentStack,
              predictiveBackParams = predictiveBackParams,
              setItems = { items = it },
          )
        }
      }
    }
  }

  @Composable
  private fun Child(
      item: AnimationItem<C, T>,
      onFinished: () -> Unit,
      content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit
  ) {
    val transition = rememberTransition(item.transitionState)

    if (item.transitionState.isIdle()) {
      LaunchedEffect(Unit) { onFinished() }
    }

    WithAnimatedVisibilityScope(transition) {
      Box(modifier = item.animator?.run { animate(item.direction) } ?: Modifier) {
        content(item.child)
      }
    }
  }

  private fun getAnimationItems(
      newStack: ChildStack<C, T>,
      oldStack: ChildStack<C, T>? = null
  ): Map<Any, AnimationItem<C, T>> =
      when {
        (oldStack == null) || (newStack.active.key == oldStack.active.key) ->
            keyedItemsOf(
                AnimationItem(
                    child = newStack.active,
                    direction = Direction.ENTER_FRONT,
                    transitionState = MutableTransitionState(EnterExitState.Visible),
                ))

        (newStack.size < oldStack.size) &&
            oldStack.backStack.any { it.key == newStack.active.key } ->
            keyedItemsOf(
                AnimationItem(
                    child = newStack.active,
                    direction = Direction.ENTER_BACK,
                    transitionState = EnterExitState.PreEnter transitionTo EnterExitState.Visible,
                    otherChild = oldStack.active,
                ),
                AnimationItem(
                    child = oldStack.active,
                    direction = Direction.EXIT_FRONT,
                    transitionState = EnterExitState.Visible transitionTo EnterExitState.PostExit,
                    otherChild = newStack.active,
                ),
            )

        else ->
            keyedItemsOf(
                AnimationItem(
                    child = oldStack.active,
                    direction = Direction.EXIT_BACK,
                    transitionState = EnterExitState.Visible transitionTo EnterExitState.PostExit,
                    otherChild = newStack.active,
                ),
                AnimationItem(
                    child = newStack.active,
                    direction = Direction.ENTER_FRONT,
                    transitionState = EnterExitState.PreEnter transitionTo EnterExitState.Visible,
                    otherChild = oldStack.active,
                ),
            )
      }

  private val ChildStack<*, *>.size: Int
    get() = items.size

  @ExperimentalDecomposeApi
  @Composable
  private fun PredictiveBackController(
      stack: ChildStack<C, T>,
      predictiveBackParams: PredictiveBackParams,
      setItems: (Map<Any, AnimationItem<C, T>>) -> Unit,
  ) {
    val scope = rememberCoroutineScope()

    val callback = remember {
      PredictiveBackCallback(
          stack = stack,
          scope = scope,
          predictiveBackParams = predictiveBackParams,
          setItems = setItems,
      )
    }

    DisposableEffect(predictiveBackParams.backHandler, callback) {
      predictiveBackParams.backHandler.register(callback)

      onDispose {
        scope.cancel() // Ensure the scope is cancelled before unregistering the callback
        predictiveBackParams.backHandler.unregister(callback)
      }
    }
  }

  private fun AnimationItem(
      child: Child.Created<C, T>,
      direction: Direction,
      transitionState: TransitionState<EnterExitState>,
      otherChild: Child.Created<C, T>,
      predictiveBackAnimator: StackAnimator? = null,
  ): AnimationItem<C, T> =
      AnimationItem(
          child = child,
          direction = direction,
          transitionState = transitionState,
          animator = predictiveBackAnimator ?: selector(child, otherChild, direction),
      )

  private inner class PredictiveBackCallback(
      private val stack: ChildStack<C, T>,
      private val scope: CoroutineScope,
      private val predictiveBackParams: PredictiveBackParams,
      private val setItems: (Map<Any, AnimationItem<C, T>>) -> Unit,
  ) : BackCallback() {
    private var animationHandler: AnimationHandler? = null
    private var initialBackEvent: BackEvent? = null

    override fun onBackStarted(backEvent: BackEvent) {
      initialBackEvent = backEvent
    }

    override fun onBackProgressed(backEvent: BackEvent) {
      startIfNeeded()

      scope.launch { animationHandler?.progress(backEvent) }
    }

    private fun startIfNeeded() {
      val backEvent = initialBackEvent ?: return
      initialBackEvent = null

      val animationHandler =
          AnimationHandler(animatable = predictiveBackParams.animatable(backEvent))
      this.animationHandler = animationHandler
      val exitChild = stack.active
      val enterChild = stack.backStack.last()

      setItems(
          keyedItemsOf(
              AnimationItem(
                  child = enterChild,
                  direction = Direction.ENTER_BACK,
                  transitionState = animationHandler.enterTransitionState,
                  otherChild = exitChild,
                  predictiveBackAnimator =
                      animationHandler.animatable?.let { anim ->
                        SimpleStackAnimator { anim.enterModifier }
                      },
              ),
              AnimationItem(
                  child = exitChild,
                  direction = Direction.EXIT_FRONT,
                  transitionState = animationHandler.exitTransitionState,
                  otherChild = enterChild,
                  predictiveBackAnimator =
                      animationHandler.animatable?.let { anim ->
                        SimpleStackAnimator { anim.exitModifier }
                      },
              ),
          ))

      scope.launch { animationHandler.progress(backEvent) }
    }

    override fun onBackCancelled() {
      initialBackEvent = null

      scope.launch {
        animationHandler?.also { handler ->
          handler.cancel()
          animationHandler = null
          setItems(getAnimationItems(newStack = stack))
        }
      }
    }

    override fun onBack() {
      initialBackEvent = null

      scope.launch {
        animationHandler?.also { handler ->
          handler.finish()
          animationHandler = null
          setItems(getAnimationItems(newStack = stack.dropLast()))
        }

        predictiveBackParams.onBack()
      }
    }
  }

  internal fun <C : Any, T : Any> ChildStack<C, T>.dropLast(): ChildStack<C, T> =
      ChildStack(active = backStack.last(), backStack = backStack.dropLast(1))

  private class AnimationHandler(
      val animatable: PredictiveBackAnimatable?,
  ) {
    val exitTransitionState: SeekableTransitionState<EnterExitState> =
        SeekableTransitionState(EnterExitState.Visible)
    val enterTransitionState: SeekableTransitionState<EnterExitState> =
        SeekableTransitionState(EnterExitState.PreEnter)

    suspend fun progress(backEvent: BackEvent) {
      animatable?.run {
        animate(backEvent)
        return@progress // Don't animate transition states on back progress if there is
                        // PredictiveBackAnimatable
      }

      awaitAll(
          {
            exitTransitionState.seekTo(
                fraction = backEvent.progress, targetState = EnterExitState.PostExit)
          },
          {
            enterTransitionState.seekTo(
                fraction = backEvent.progress, targetState = EnterExitState.Visible)
          },
      )
    }

    suspend fun cancel() {
      awaitAll(
          { exitTransitionState.snapTo(EnterExitState.Visible) },
          { enterTransitionState.snapTo(EnterExitState.PreEnter) },
          { animatable?.cancel() },
      )
    }

    suspend fun finish() {
      awaitAll(
          { exitTransitionState.animateTo(EnterExitState.PostExit) },
          { enterTransitionState.animateTo(EnterExitState.Visible) },
          { animatable?.finish() },
      )
    }
  }
}

@Composable
private fun Overlay(modifier: Modifier) {
  Box(
      modifier =
          modifier.pointerInput(Unit) {
            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
              }
            }
          },
  )
}

@ExperimentalDecomposeApi
private data class AnimationItem<out C : Any, out T : Any>(
    val child: Child.Created<C, T>,
    val direction: Direction,
    val transitionState: TransitionState<EnterExitState>,
    val animator: StackAnimator? = null,
)

@ExperimentalDecomposeApi
private fun <C : Any, T : Any> keyedItemsOf(
    vararg items: AnimationItem<C, T>
): Map<Any, AnimationItem<C, T>> = items.associateBy { it.child.key }

/*
 * Can't be anonymous. See:
 * https://github.com/JetBrains/compose-jb/issues/2688
 * https://github.com/JetBrains/compose-jb/issues/2612
 */
@ExperimentalDecomposeApi
private class SimpleStackAnimator(
    private val modifier: () -> Modifier,
) : StackAnimator {
  @Composable
  override fun AnimatedVisibilityScope.animate(direction: Direction): Modifier = modifier()
}

private infix fun <S> S.transitionTo(targetState: S): MutableTransitionState<S> =
    MutableTransitionState(this).apply { this.targetState = targetState }

private fun TransitionState<*>.isIdle(): Boolean =
    when (this) {
      is MutableTransitionState -> isIdle
      is SeekableTransitionState -> false
      else -> false
    }

internal suspend inline fun awaitAll(vararg jobs: suspend CoroutineScope.() -> Unit) {
  coroutineScope { jobs.map { launch(block = it) }.joinAll() }
}



/**
 * An implementation of [ChildPanelsLayout] for laying out panels in the following ways.
 *
 * - If the `mode` is `SINGLE`, all panels are displayed in a stack. The Main panel, then
 * the Details panel on top (if any), and finally the Extra panel (if any).
 * - If the `mode` is `DUAL`, the Main panel is always displayed on the left side, and then
 * the Details and the Extra panels are displayed in a stack on the right side (next to the Main panel).
 * - If the `mode` is `TRIPLE`, all panels are displayed horizontally side by side.
 *
 * ```
 * SINGLE mode
 * +-----------------------------+
 * |            Main             |
 * |           Details           |
 * |            Extra            |
 * +-----------------------------+
 *
 * DUAL mode
 * +-----------------------------+
 * |              |    Details   |
 * |     Main     |     Extra    |
 * |              |              |
 * +-----------------------------+
 *
 * TRIPLE mode
 * +-----------------------------+
 * |         |         |         |
 * |  Main   | Details |  Extra  |
 * |         |         |         |
 * +-----------------------------+
 * ```
 *
 * @param dualWeights a [Pair] of weights for two panels to be used in
 * [DUAL][com.arkivanov.decompose.router.panels.ChildPanelsMode.DUAL] mode.
 * Default values are `1F`, meaning that all panels have equal width.
 * @param tripleWeights A [Triple] of weights for three panels to be used in
 * [TRIPLE][com.arkivanov.decompose.router.panels.ChildPanelsMode.TRIPLE] mode.
 * Default values are `1F`, meaning that all panel slots have equal width.
 */
@ExperimentalDecomposeApi
class HorizontalChildPanelsLayout(
    private val dualWeights: Pair<Float, Float> = Pair(1F, 1F),
    private val tripleWeights: Triple<Float, Float, Float> = Triple(1F, 1F, 1F),
) : ChildPanelsLayout {

    private val singleMeasurePolicy = SingleMeasurePolicy()
    private val dualMeasurePolicy = DualMeasurePolicy(weights = dualWeights)
    private val tripleMeasurePolicy = TripleMeasurePolicy(weights = tripleWeights)

    @Composable
    override fun Layout(
        mode: ChildPanelsMode,
        main: @Composable () -> Unit,
        details: @Composable () -> Unit,
        extra: @Composable () -> Unit,
    ) {
        androidx.compose.ui.layout.Layout(
            content = {
                main()
                details()
                extra()
            },
            modifier = Modifier.fillMaxSize(),
            measurePolicy = when (mode) {
                ChildPanelsMode.SINGLE -> singleMeasurePolicy
                ChildPanelsMode.DUAL -> dualMeasurePolicy
                ChildPanelsMode.TRIPLE -> tripleMeasurePolicy
            },
        )
    }

    private class SingleMeasurePolicy : MeasurePolicy {
        override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
            val placeables = measurables.map { it.measure(constraints) }

            return layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach {
                    it.placeRelative(x = 0, y = 0)
                }
            }
        }
    }

    private class DualMeasurePolicy(weights: Pair<Float, Float>) : MeasurePolicy {
        private val primaryWeight = weights.first / (weights.first + weights.second)

        override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
            val w1 = (constraints.maxWidth.toFloat() * primaryWeight).toInt()
            val w2 = constraints.maxWidth - w1
            val placeable1 = measurables[0].measure(constraints.copy(maxWidth = w1, minWidth = w1))
            val placeable2 = measurables[1].measure(constraints.copy(maxWidth = w2, minWidth = w2))
            val placeable3 = measurables[2].measure(constraints.copy(maxWidth = w2, minWidth = w2))

            return layout(constraints.maxWidth, constraints.maxHeight) {
                placeable1.placeRelative(x = 0, y = 0)
                placeable2.placeRelative(x = w1, y = 0)
                placeable3.placeRelative(x = w1, y = 0)
            }
        }
    }

    private class TripleMeasurePolicy(weights: Triple<Float, Float, Float>) : MeasurePolicy {
        private val primaryWeight = weights.first / (weights.first + weights.second + weights.third)
        private val secondaryWeight = weights.second / (weights.first + weights.second + weights.third)

        override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
            val w1 = (constraints.maxWidth.toFloat() * primaryWeight).toInt()
            val w2 = (constraints.maxWidth.toFloat() * secondaryWeight).toInt()
            val w3 = (constraints.maxWidth - w1 - w2)
            val placeable1 = measurables[0].measure(constraints.copy(maxWidth = w1, minWidth = w1))
            val placeable2 = measurables[1].measure(constraints.copy(maxWidth = w2, minWidth = w2))
            val placeable3 = measurables[2].measure(constraints.copy(maxWidth = w3, minWidth = w3))

            return layout(constraints.maxWidth, constraints.maxHeight) {
                placeable1.placeRelative(x = 0, y = 0)
                placeable2.placeRelative(x = w1, y = 0)
                placeable3.placeRelative(x = w1 + w2, y = 0)
            }
        }
    }
}
