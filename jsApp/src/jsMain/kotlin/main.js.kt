import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.jetbrains.skiko.wasm.onWasmReady
import uz.rsmax.kotlin210rctest.GreetingView
import uz.rsmax.kotlin210rctest.Test210RCComponent
import uz.rsmax.kotlin210rctest.getPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  onWasmReady {

    Test210RCComponent().onDisposeChanges()

    CanvasBasedWindow(canvasElementId = "compose-content") {
      GreetingView(getPlatform().name)
    }
  }
}