import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.webhistory.enableWebHistoryNavigation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.mvikotlin.logging.logger.Logger
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.jetbrains.skiko.wasm.onWasmReady
import uz.rsmax.kotlin210rctest.decompose.DefaultAppComponentContext
import uz.rsmax.kotlin210rctest.decompose.database.DatabaseMigrator
import uz.rsmax.kotlin210rctest.decompose.root.RootComponent
import uz.rsmax.kotlin210rctest.decomposeui.root.RootContent
import web.dom.DocumentVisibilityState
import web.dom.document
import web.events.EventType
import web.events.addEventListener

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val lifecycle = LifecycleRegistry()

  Napier.base(DebugAntilog())

  val componentContext = DefaultComponentContext(lifecycle)
  val appComponentContext = DefaultAppComponentContext(
    componentContext = componentContext,
    storeFactory = LoggingStoreFactory(
      delegate = DefaultStoreFactory(),
      logger = object : Logger {
        override fun log(text: String) {
          Napier.d("MVIKotlin: $text")
        }
      },
    ),
  )


  onWasmReady {
//    Test210RCComponent().onDisposeChanges()

    CanvasBasedWindow(canvasElementId = "compose-content") {
      val databaseIsReady by initializeDatabase(appComponentContext.migrator)
      if (databaseIsReady) {
        val root = remember {
          RootComponent(appComponentContext).also {
            lifecycle.attachToDocument()
            enableWebHistoryNavigation(it)
          }
        }
        RootContent(root)
      }
    }
  }
}

@Composable
fun initializeDatabase(databaseWrapper: DatabaseMigrator): State<Boolean> {
  val databaseIsReady = remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    databaseWrapper.migrateIfNeeded()
    databaseIsReady.value = true
  }
  return databaseIsReady
}

fun LifecycleRegistry.attachToDocument() {
  fun onVisibilityChanged() {
    if (document.visibilityState == DocumentVisibilityState.visible) {
      resume()
    } else {
      stop()
    }
  }

  onVisibilityChanged()

  document.addEventListener(
    type = EventType("visibilitychange"),
    handler = { onVisibilityChanged() },
  )
}
