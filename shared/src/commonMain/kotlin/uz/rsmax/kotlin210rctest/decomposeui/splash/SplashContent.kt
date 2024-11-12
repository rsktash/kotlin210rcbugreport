package uz.rsmax.kotlin210rctest.decomposeui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.decompose.splash.SplashComponent


@Composable
fun SplashContent(
  component: SplashComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.model.subscribeAsState()
  Box(modifier.fillMaxSize(), Alignment.Center) {
    if (state.loading) {
      CircularProgressIndicator()
    }
  }
}