package uz.rsmax.kotlin210rctest.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.logging.logger.Logger
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import uz.rsmax.kotlin210rctest.GreetingView
import uz.rsmax.kotlin210rctest.decompose.DefaultAppComponentContext
import uz.rsmax.kotlin210rctest.decompose.initContext
import uz.rsmax.kotlin210rctest.decompose.root.RootComponent
import uz.rsmax.kotlin210rctest.decomposeui.root.RootContent

class MainActivity : ComponentActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initContext(this)
    Napier.base(DebugAntilog())


    val componentContext = defaultComponentContext()
    val appComponentContext = DefaultAppComponentContext(
      componentContext = componentContext,
      storeFactory = LoggingStoreFactory(
        delegate = DefaultStoreFactory(),
        logger = object : Logger {
          override fun log(text: String) {
            Napier.d("MVIKotlin: $text")
          }
        },
      )
    )
    val root = RootComponent(appComponentContext)
    setContent {
      RootContent(root)
    }
  }
}

@Preview
@Composable
fun DefaultPreview() {
  MyApplicationTheme {
    GreetingView("Hello, Android!")
  }
}
