package uz.rsmax.kotlin210rctest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.util.fastFirst

@Composable
fun NavigationSuiteScaffold(
  navigationSuiteItems: NavigationSuiteScope.() -> Unit,
  modifier: Modifier = Modifier,
  layoutType: NavigationSuiteType =
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(WindowAdaptiveInfoDefault),
  navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
  containerColor: Color = NavigationSuiteScaffoldDefaults.containerColor,
  contentColor: Color = NavigationSuiteScaffoldDefaults.contentColor,
  content: @Composable (Modifier) -> Unit = {},
) {


  Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
    NavigationSuiteScaffoldLayout(
      navigationSuite = {
        NavigationSuite(
          layoutType = layoutType,
          colors = navigationSuiteColors,
          content = navigationSuiteItems
        )
      },
      layoutType = layoutType,
      content = {
        content(
          Modifier.consumeWindowInsets(
            when (layoutType) {
              NavigationSuiteType.NavigationBar -> NavigationBarDefaults.windowInsets
              NavigationSuiteType.NavigationRail -> NavigationRailDefaults.windowInsets
              NavigationSuiteType.NavigationDrawer -> DrawerDefaults.windowInsets
              else -> WindowInsets(0, 0, 0, 0)
            }
          )
        )
      })
  }
}

/**
 * Layout for a [NavigationSuiteScaffold]'s content. This function wraps the [content] and places
 * the [navigationSuite] component according to the given [layoutType].
 *
 * The usage of this function is recommended when you need some customization that is not viable via
 * the use of [NavigationSuiteScaffold]. Example usage:
 *
 * @sample androidx.compose.material3.adaptive.navigationsuite.samples.NavigationSuiteScaffoldCustomNavigationRail
 * @param navigationSuite the navigation component to be displayed, typically [NavigationSuite]
 * @param layoutType the current [NavigationSuiteType]. Defaults to
 *   [NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo]
 * @param content the content of your screen
 */
@Composable
fun NavigationSuiteScaffoldLayout(
  navigationSuite: @Composable () -> Unit,
  layoutType: NavigationSuiteType =
    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(WindowAdaptiveInfoDefault),
  content: @Composable () -> Unit = {}
) {
  Layout({
    // Wrap the navigation suite and content composables each in a Box to not propagate the
    // parent's (Surface) min constraints to its children (see b/312664933).
    Box(Modifier.layoutId(NavigationSuiteLayoutIdTag)) { navigationSuite() }
    Box(Modifier.layoutId(ContentLayoutIdTag)) { content() }
  }) { measurables, constraints ->
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    // Find the navigation suite composable through it's layoutId tag
    val navigationPlaceable =
      measurables
        .fastFirst { it.layoutId == NavigationSuiteLayoutIdTag }
        .measure(looseConstraints)
    val isNavigationBar = layoutType == NavigationSuiteType.NavigationBar
    val layoutHeight = constraints.maxHeight
    val layoutWidth = constraints.maxWidth
    // Find the content composable through it's layoutId tag
    val contentPlaceable =
      measurables
        .fastFirst { it.layoutId == ContentLayoutIdTag }
        .measure(
          if (isNavigationBar) {
            constraints.copy(
              minHeight = layoutHeight - navigationPlaceable.height,
              maxHeight = layoutHeight - navigationPlaceable.height
            )
          } else {
            constraints.copy(
              minWidth = layoutWidth - navigationPlaceable.width,
              maxWidth = layoutWidth - navigationPlaceable.width
            )
          }
        )

    layout(layoutWidth, layoutHeight) {
      if (isNavigationBar) {
        // Place content above the navigation component.
        contentPlaceable.placeRelative(0, 0)
        // Place the navigation component at the bottom of the screen.
        navigationPlaceable.placeRelative(0, layoutHeight - (navigationPlaceable.height))
      } else {
        // Place the navigation component at the start of the screen.
        navigationPlaceable.placeRelative(0, 0)
        // Place content to the side of the navigation component.
        contentPlaceable.placeRelative((navigationPlaceable.width), 0)
      }
    }
  }
}

internal val WindowAdaptiveInfoDefault
  @Composable get() = currentWindowAdaptiveInfo()

private const val NavigationSuiteLayoutIdTag = "navigationSuite"
private const val ContentLayoutIdTag = "content"
