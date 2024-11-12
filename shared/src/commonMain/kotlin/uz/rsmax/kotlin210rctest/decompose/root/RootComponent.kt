package uz.rsmax.kotlin210rctest.decompose.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigationOwner
import com.arkivanov.decompose.router.webhistory.ofStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import uz.rsmax.kotlin210rctest.decompose.AppComponentContext
import uz.rsmax.kotlin210rctest.decompose.AppUser
import uz.rsmax.kotlin210rctest.decompose.BaseRouterComponent
import uz.rsmax.kotlin210rctest.decompose.app.AppComponent
import uz.rsmax.kotlin210rctest.decompose.app.DefaultUserComponentContext
import uz.rsmax.kotlin210rctest.decompose.login.LoginComponent
import uz.rsmax.kotlin210rctest.decompose.root.RootComponent.Child
import uz.rsmax.kotlin210rctest.decompose.splash.SplashComponent

fun RootComponent(
  componentContext: AppComponentContext
): RootComponent = RootComponentImpl(componentContext)

interface RootComponent : BaseRouterComponent<Child>, WebHistoryNavigationOwner {
  sealed interface Child {
    data class Splash(val component: SplashComponent) : Child
    data class Login(val component: LoginComponent) : Child
    data class App(val component: AppComponent) : Child
  }
}

internal class RootComponentImpl(
  componentContext: AppComponentContext
) : RootComponent,
  AppComponentContext by componentContext {

  private val navigation = StackNavigation<Configuration>()

  private val _stack = childStack(
    source = navigation,
    serializer = Configuration.serializer(),
    initialStack = { listOf(Configuration.Splash) },
    childFactory = ::createChild,
  )

  override val stack: Value<ChildStack<*, Child>> = _stack

  override val webHistoryNavigation: WebHistoryNavigation<*> = WebHistoryNavigation.ofStack(
    navigator = navigation,
    stack = _stack,
    serializer = Configuration.serializer(),
    isHistoryEnabled = false,
    pathMapper = {
      when (it.configuration) {
        is Configuration.Splash -> "splash"
        is Configuration.Login -> "login"
        is Configuration.App -> "app"
      }
    },
    childLocator = {
      when (val child = it.instance) {
        is Child.App -> child.component
        is Child.Login -> null
        is Child.Splash -> null
      }
    }
  )

  private fun createChild(
    configuration: Configuration,
    context: AppComponentContext
  ): Child = when (configuration) {
    is Configuration.Splash -> Child.Splash(splash(context))
    is Configuration.Login -> Child.Login(login(context))
    is Configuration.App -> Child.App(app(context, configuration.user))
  }

  private fun splash(context: AppComponentContext): SplashComponent =
    SplashComponent(
      componentContext = context,
      dependencies = object : SplashComponent.Dependencies {
        override val output: suspend (SplashComponent.Output) -> Unit = { out ->
          when (out) {
            is SplashComponent.Output.OnUserFound ->
              navigation.navigate { listOf(Configuration.App(out.user)) }

            is SplashComponent.Output.OnUserNotFound ->
              navigation.navigate { listOf(Configuration.Login) }
          }
        }
      },
    )

  private fun login(context: AppComponentContext): LoginComponent =
    LoginComponent(
      componentContext = context,
      dependencies = object : LoginComponent.Dependencies {
        override val output: suspend (LoginComponent.Output) -> Unit = { out ->
          when (out) {
            is LoginComponent.Output.OnUserLoggedIn ->
              navigation.navigate { listOf(Configuration.App(out.user)) }
          }
        }
      },
    )

  private fun app(context: AppComponentContext, user: AppUser): AppComponent =
    AppComponent(
      componentContext = DefaultUserComponentContext(context, user)
    )


  @Serializable
  private sealed interface Configuration {
    @Serializable
    data object Splash : Configuration

    @Serializable
    data object Login : Configuration

    @Serializable
    data class App(val user: AppUser) : Configuration
  }

}