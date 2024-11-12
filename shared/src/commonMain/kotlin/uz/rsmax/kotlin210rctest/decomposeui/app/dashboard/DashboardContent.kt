package uz.rsmax.kotlin210rctest.decomposeui.app.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import uz.rsmax.kotlin210rctest.decompose.app.dashboard.DashboardComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
  component: DashboardComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.model.subscribeAsState()
  Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text(text = "Dashboard") }) },
    content = { paddingValues ->
      LazyColumn(Modifier.padding(paddingValues)) {
        items(state.notifications) { notification ->
          ListItem(
            headlineContent = { Text(text = notification) }
          )
        }
      }
    }
  )
}