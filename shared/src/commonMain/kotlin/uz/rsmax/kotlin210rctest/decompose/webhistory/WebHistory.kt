package com.arkivanov.decompose.router.webhistory

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.PanelsNavigator
import com.arkivanov.decompose.router.panels.navigate
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation.HistoryItem
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.math.abs

interface WebHistoryNavigationOwner {

  val webHistoryNavigation: WebHistoryNavigation<*>
}

interface WebHistoryNavigation<T : HistoryItem.Data> {

  val serializer: KSerializer<T>
  val history: Value<List<HistoryItem<T>>>

  fun navigate(history: List<T>)

  class HistoryItem<out T : HistoryItem.Data>(
      val data: T,
      val key: Any,
      val child: WebHistoryNavigationOwner?, // TODO: Rename?
  ) {
    interface Data {
      val path: String
      val parameters: Map<String, String>
    }
  }

  companion object
}

object NoOpWebHistoryNavigationOwner : WebHistoryNavigationOwner {
  override val webHistoryNavigation: WebHistoryNavigation<*>
    get() {
      throw NotImplementedError("Not implemented")
    }
}

fun <C : Any, T : Any> WebHistoryNavigation.Companion.ofStack(
    navigator: StackNavigator<C>,
    stack: Value<ChildStack<C, T>>,
    serializer: KSerializer<C>,
    isHistoryEnabled: Boolean = true,
    pathMapper: (Child.Created<C, T>) -> String = { it.configuration.path() },
    parametersMapper: (Child.Created<C, T>) -> Map<String, String> = { emptyMap() },
    childLocator: (Child.Created<C, T>) -> WebHistoryNavigationOwner? = { null },
): WebHistoryNavigation<*> =
    StackWebHistoryNavigation(
        navigator = navigator,
        stack = stack,
        serializer = serializer,
        isHistoryEnabled = isHistoryEnabled,
        pathMapper = pathMapper,
        parametersMapper = parametersMapper,
        childLocator = childLocator,
    )

// TODO: Use serial name or typeOf?
private fun Any.path(): String = this::class.simpleName ?: hashCode().toStringPretty()

private fun Int.toStringPretty(): String =
    abs(this).toString(radix = 36).let { str -> if (this >= 0) str else "x$str" }

private class StackWebHistoryNavigation<C : Any, T : Any>(
    private val navigator: StackNavigator<C>,
    stack: Value<ChildStack<C, T>>,
    serializer: KSerializer<C>,
    private val isHistoryEnabled: Boolean, // TODO: Rename?
    private val pathMapper: (Child.Created<C, T>) -> String,
    private val parametersMapper: (Child.Created<C, T>) -> Map<String, String>,
    private val childLocator: (Child.Created<C, T>) -> WebHistoryNavigationOwner?,
) : WebHistoryNavigation<StackWebHistoryNavigation.HistoryItemData<C>> {

  override val serializer: KSerializer<HistoryItemData<C>> = HistoryItemData.serializer(serializer)
  override val history: Value<List<HistoryItem<HistoryItemData<C>>>> = stack.map { it.toHistory() }

  private fun ChildStack<C, T>.toHistory(): List<HistoryItem<HistoryItemData<C>>> =
      (if (isHistoryEnabled) items else items.takeLast(1)).map { child ->
        HistoryItem(
            data =
                HistoryItemData(
                    path = pathMapper(child),
                    parameters = parametersMapper(child),
                    configuration = child.configuration,
                ),
            key = child.key,
            child = childLocator(child),
        )
      }

  override fun navigate(history: List<HistoryItemData<C>>) {
    navigator.navigate { history.map { it.configuration } }
  }

  @Serializable
  data class HistoryItemData<out C : Any>(
      override val path: String,
      override val parameters: Map<String, String>,
      val configuration: C,
  ) : HistoryItem.Data
}

fun <MC : Any, MT : Any, DC : Any, DT : Any, EC : Any, ET : Any> WebHistoryNavigation.Companion
    .ofPanels(
    navigator: PanelsNavigator<MC, DC, EC>,
    panels: Value<ChildPanels<MC, MT, DC, DT, EC, ET>>,
    mainSerializer: KSerializer<MC>,
    detailsSerializer: KSerializer<DC>,
    extraSerializer: KSerializer<EC>,
    pathMapper: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> String = { "" },
    parametersMapper: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> Map<String, String> = { emptyMap() },
    childLocator: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> WebHistoryNavigationOwner? = {
      val detailsComponent = it.details?.instance
      val extraComponent = it.extra?.instance
      when {
        extraComponent is WebHistoryNavigationOwner -> extraComponent
        detailsComponent is WebHistoryNavigationOwner -> detailsComponent
        else -> null
      }
    },
): WebHistoryNavigation<*> =
    PanelsWebHistoryNavigation(
        navigator = navigator,
        panels = panels,
        mainSerializer = mainSerializer,
        detailsSerializer = detailsSerializer,
        extraSerializer = extraSerializer,
        pathMapper = pathMapper,
        parametersMapper = parametersMapper,
        childLocator = childLocator,
    )

private class PanelsWebHistoryNavigation<
    MC : Any, out MT : Any, DC : Any, out DT : Any, EC : Any, out ET : Any>(
    private val navigator: PanelsNavigator<MC, DC, EC>,
    panels: Value<ChildPanels<MC, MT, DC, DT, EC, ET>>,
    mainSerializer: KSerializer<MC>,
    detailsSerializer: KSerializer<DC>,
    extraSerializer: KSerializer<EC>,
    private val pathMapper: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> String,
    private val parametersMapper: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> Map<String, String>,
    private val childLocator: (ChildPanels<MC, MT, DC, DT, EC, ET>) -> WebHistoryNavigationOwner?
) : WebHistoryNavigation<PanelsWebHistoryNavigation.HistoryItemData<MC, DC, EC>> {

  override val serializer: KSerializer<HistoryItemData<MC, DC, EC>> =
      HistoryItemData.serializer(mainSerializer, detailsSerializer, extraSerializer)

  override val history: Value<List<HistoryItem<HistoryItemData<MC, DC, EC>>>> =
      panels.map { panels ->
        listOf(
            HistoryItem(
                data =
                    HistoryItemData(
                        path = pathMapper(panels),
                        parameters = parametersMapper(panels),
                        main = panels.main.configuration,
                        details = panels.details?.configuration,
                        extra = panels.extra?.configuration,
                    ),
                key = Triple(panels.main.key, panels.details?.key, panels.extra?.key),
                child = childLocator(panels),
            ),
        )
      }

  override fun navigate(history: List<HistoryItemData<MC, DC, EC>>) {
    val data = history.single()
    navigator.navigate(main = data.main, details = data.details, extra = data.extra)
  }

  @Serializable
  data class HistoryItemData<out MC : Any, out DC : Any, out EC : Any>(
      override val path: String,
      override val parameters: Map<String, String>,
      val main: MC,
      val details: DC?,
      val extra: EC?,
  ) : HistoryItem.Data
}
