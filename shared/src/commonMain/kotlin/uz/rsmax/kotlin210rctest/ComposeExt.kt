package uz.rsmax.kotlin210rctest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import uz.rsmax.base.EditableData
import uz.rsmax.kotlin210rctest.decompose.BaseNavBackEvents
import uz.rsmax.kotlin210rctest.decompose.LoadableData
import uz.rsmax.kotlin210rctest.decompose.NavigateBackState
import uz.rsmax.kotlin210rctest.decomposeui.icons.IconError

@Composable
fun rememberTextFieldStateAndUpdate(
  value: String,
  onValueChange: (String) -> Unit,
): TextFieldState {
  val state = rememberTextFieldState(value)

  LaunchedEffect(value) {
    if (state.text.toString() != value) {
      state.edit { replace(0, length, value) }
    }
  }
  LaunchedEffect(Unit) {
    snapshotFlow { state.text }.drop(1).collectLatest { onValueChange(it.toString()) }
  }

  return state
}

@Composable
fun isMultiPane(): Boolean {
  val componentSize = calculateSizeFromAdaptiveInfo(currentWindowAdaptiveInfo())
  //  val isBeingEdited = panels.detailsIsBeingEdited()
  return componentSize == ComponentSize.Expanded /*&& !isBeingEdited*/
}

enum class ComponentSize {
  Compact,
  Medium,
  Expanded,
}

fun calculateSizeFromAdaptiveInfo(adaptiveInfo: WindowAdaptiveInfo): ComponentSize {
  return with(adaptiveInfo) {
    when {
      windowPosture.isTabletop ||
          windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT ->
        ComponentSize.Compact

      windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> ComponentSize.Medium
      windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ->
        ComponentSize.Expanded

      else -> ComponentSize.Compact
    }
  }
}


@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun rememberHorizontalChildPanelsLayout(): HorizontalChildPanelsLayout = remember {
  HorizontalChildPanelsLayout(
    dualWeights = Pair(first = 0.61F, second = 0.39F),
    tripleWeights = Triple(first = 0.5F, second = 0.3F, third = 0.2F),
  )
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun <T> rememberPredictiveBackParams(component: T):
      (ChildPanels<*, *, *, *, *, *>) -> PredictiveBackParams? where
    T : BackHandlerOwner,
    T : BaseNavBackEvents {
  return remember {
    {
      PredictiveBackParams(
        backHandler = component.backHandler,
        onBack = component::onNavigateBack,
        animatable = ::materialPredictiveBackAnimatable,
      )
    }
  }
}
@OptIn(ExperimentalDecomposeApi::class)
fun materialPredictiveBackAnimatable(
  initialBackEvent: BackEvent,
  shape: ((progress: Float, edge: BackEvent.SwipeEdge) -> Shape)? = { progress, edge ->
    RoundedCornerShape(
      corner = CornerSize(48.dp * progress)
    )
  },
): PredictiveBackAnimatable =
  com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable(
    initialBackEvent = initialBackEvent,
    shape = shape,
  )

@Composable
fun <T : Any> LoadingContent(
  data: LoadableData<T>,
  modifier: Modifier = Modifier,
  content: @Composable (T) -> Unit,
) {
  Box(modifier) {
    when (data) {
      is LoadableData.Idle -> {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is LoadableData.Loaded -> {
        content(data.data)
      }

      is LoadableData.Error -> {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
          Column {
            Icon(imageVector = Icons.Outlined.IconError, contentDescription = null)
            SelectionContainer { Text(text = "${data.error.message}") }
          }
        }
      }
    }
  }
}

@Composable
fun NavBackButton(
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  IconButton(
    onClick = onClick,
    content = { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) },
    enabled = enabled,
  )
}

@Composable
fun NavigateBackState.NavBackButton(onClick: () -> Unit) {
  if (canNavigateBack) NavBackButton(onClick, canNavigateBack)
}


@Composable
fun LabeledText(
  label: String,
  textAlign: TextAlign? = null,
  modifier: Modifier = Modifier,
  text: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    text()
    CompositionLocalProvider(
      LocalContentColor provides LocalContentColor.current.copy(alpha = 0.7f)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
      )
    }
  }
}


@Composable
fun <T : Any> EditingContent(
  data: EditableData<T>,
  onDisposeChanges: () -> Unit,
  onCancelDispose: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (T) -> Unit,
) {
  Box(modifier) {
    when (data) {
      EditableData.Idle -> {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
      }

      is EditableData.Loaded -> {
        content(data.data)
        if (data.promtDisposeChanges) {
          DisposeChangesDialog(onDisposeChanges, onCancelDispose)
        }
      }

      is EditableData.Error -> {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
          Column {
            Icon(imageVector = Icons.Outlined.IconError, contentDescription = null)
            SelectionContainer { Text(text = "${data.error.message}") }
          }
        }
      }
    }
  }
}

@Composable
private fun DisposeChangesDialog(
  onDisposeChanges: () -> Unit,
  onCancelDispose: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onCancelDispose,
    text = { Text(text = "Do you want to dispose changes?") },
    dismissButton = {
      TextButton(onClick = onCancelDispose, content = { Text(text = "Cancel") })
    },
    confirmButton = {
      TextButton(onClick = onDisposeChanges, content = { Text(text = "Dispose") })
    },
  )
}


@OptIn(ExperimentalDecomposeApi::class)
fun ChildPanels<*, *, *, *, *, *>.showNavigation(): Boolean =
  mode != ChildPanelsMode.SINGLE || details == null

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun Value<ChildPanels<*, *, *, *, *, *>>.showNavigation(): Boolean {
  return subscribeAsStateDelayed().value.showNavigation()
}

@Composable
fun <T : Any> Value<T>.subscribeAsStateDelayed(
  delayMillis: Long = 50,
  policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
): State<T> {
  val scope = rememberCoroutineScope()
  val state = remember(this, policy) { mutableStateOf(value, policy) }

  DisposableEffect(delayMillis) {
    val disposable = subscribe {
      scope.launch {
        delay(delayMillis)
        state.value = it
      }
    }
    onDispose { disposable.cancel() }
  }

  return state
}

fun Modifier.contentPadding(): Modifier = padding(horizontal = 12.dp)