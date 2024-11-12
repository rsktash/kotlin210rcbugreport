@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.arkivanov.decompose.router.webhistory

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.Json
import com.arkivanov.decompose.router.stack.startsWith
import com.arkivanov.decompose.router.stack.subscribe
import com.arkivanov.decompose.router.webhistory.WebHistoryNavigation.HistoryItem
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

fun enableWebHistoryNavigation(owner: WebHistoryNavigationOwner) {
  enableWebHistoryNavigation(owner.webHistoryNavigation)
}

fun <T : HistoryItem.Data> enableWebHistoryNavigation(
    navigation: WebHistoryNavigation<T>,
) {
  val oldHistory = navigation.history.value

  replaceState(oldHistory.map { it.tree(navigation.serializer) })

  var isEnabled = true

  navigation.subscribe(
      isEnabled = { isEnabled },
      onPush = { history -> history.forEach(::pushState) },
      onPop = ::go,
      onRewrite = { oldSize, newHistory ->
        if (oldSize > 1) {
          val onPopState = window.onpopstate

          window.onpopstate = {
            window.onpopstate = onPopState
            replaceState(newHistory.first())
            newHistory.drop(1).forEach(::pushState)
          }

          window.history.go(-oldSize + 1)
        } else {
          replaceState(newHistory.first())
          newHistory.drop(1).forEach(::pushState)
        }
      },
      onUpdateUrl = { nodes ->

        console.log("WebHistory | onUpdateUrl: ${nodes.last().url()}, locaiton: ${window.location.pathname}")
        window.history.replaceState(
            data = window.history.state,
            title = "",
            url = nodes.last().url(),
        )
      },
  )

  window.onpopstate = { event ->
    isEnabled = false
    val nodes = event.state.unsafeCast<String>().deserializeNodes()
    navigation.navigate(nodes)
    isEnabled = true
    Unit
  }
}

private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.navigate(nodes: List<SerializableNode>) {
  val items: List<T> = nodes.map { it.data.consumeRequired(serializer) }
  navigate(items)

  history.value.forEachIndexed { index, item ->
    item.child?.webHistoryNavigation?.navigate(nodes[index].children)
  }
}

private fun replaceState(nodes: List<Node<*>>) {
  val url = nodes.last().url().replaceFirst(window.location.pathname, "")
  console.log("WebHistory | replaceState: $url, locaiton: ${window.location.pathname}")
  console.log("WebHistory | replaceState: $nodes")

  window.history.replaceState(
      data = nodes.serialize(),
      title = "",
      url = url,
  )
}

private fun pushState(nodes: List<Node<*>>) {
  val url = nodes.last().url().replaceFirst(window.location.pathname, "")
  console.log("WebHistory | pushState: $url, locaiton: ${window.location.pathname}")
  console.log("WebHistory | pushState: $nodes")

  window.history.pushState(
      data = nodes.serialize(),
      title = "",
      url = url,
  )
}

private fun <T : HistoryItem.Data> Node<T>.toSerializableNode(): SerializableNode =
    SerializableNode(
        data = SerializableContainer(value = data, strategy = serializer),
        children = children.map { it.toSerializableNode() },
    )

private fun List<Node<*>>.serialize(): String =
    Json.encodeToString(
        serializer = ListSerializer(SerializableNode.serializer()),
        value = map { it.toSerializableNode() },
    )

private fun String.deserializeNodes(): List<SerializableNode> =
    Json.decodeFromString(
        deserializer = ListSerializer(SerializableNode.serializer()),
        string = this,
    )

private fun String.deserializeNode(): SerializableNode =
    Json.decodeFromString(string = this, deserializer = SerializableNode.serializer())

private fun go(delta: Int) {
  console.log("WebHistory | go: $delta")
  window.history.go(delta = delta)
}

private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.subscribe(
    isEnabled: () -> Boolean,
    onPush: (List<List<Node<T>>>) -> Unit,
    onPop: (delta: Int) -> Unit,
    onRewrite: (oldSize: Int, newHistory: List<List<Node<T>>>) -> Unit,
    onUpdateUrl: (List<Node<T>>) -> Unit,
): Cancellation {
  var activeChildCancellation: Cancellation? = null

  return history.subscribe { newHistory, oldHistory ->
    activeChildCancellation?.cancel()

    if (isEnabled()) {
      onHistoryChanged(newHistory, oldHistory, onPush, onPop, onRewrite, onUpdateUrl)
    }

    val activeItem = newHistory.last()
    val inactiveNodes = newHistory.dropLast(1).map { it.tree(serializer) }

    activeChildCancellation =
        activeItem.child
            ?.webHistoryNavigation
            ?.subscribe(
                isEnabled = isEnabled,
                onPush = { childHistory ->
                  onPush(
                      childHistory.map { childNodes ->
                        inactiveNodes +
                            Node(
                                data = activeItem.data,
                                serializer = serializer,
                                children = childNodes,
                            )
                      })
                },
                onPop = onPop,
                onRewrite = { oldSize, childHistory ->
                  onRewrite(
                      oldSize,
                      childHistory.map { childNodes ->
                        inactiveNodes +
                            Node(
                                data = activeItem.data,
                                serializer = serializer,
                                children = childNodes,
                            )
                      })
                },
                onUpdateUrl = { childNodes ->
                  onUpdateUrl(
                      inactiveNodes +
                          Node(
                              data = activeItem.data,
                              serializer = serializer,
                              children = childNodes,
                          ))
                },
            )
  }
}

private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.onHistoryChanged(
    newHistory: List<HistoryItem<T>>,
    oldHistory: List<HistoryItem<T>>,
    onPush: (List<List<Node<T>>>) -> Unit,
    onPop: (delta: Int) -> Unit,
    onRewrite: (oldSize: Int, newHistory: List<List<Node<T>>>) -> Unit,
    onUpdateUrl: (List<Node<T>>) -> Unit,
) {
  val newKeys = newHistory.map { it.key }
  val oldKeys = oldHistory.map { it.key }

  console.log("WebHistory | Old: $oldKeys")
  console.log("WebHistory | New: $newKeys")

  when {
    newKeys == oldKeys -> { // History is not changed, but path or parameters might be
      console.log("WebHistory | when not changed")
      onUpdateUrl(newHistory.map { it.tree(serializer) })
    }

    newKeys.startsWith(oldKeys) -> { // Items pushed
      console.log("WebHistory | when items pushed")
      val newItems = newHistory.takeLast(newHistory.size - oldHistory.size)
      val historyChange = ArrayList<List<Node<T>>>()
      val previousNodes = oldHistory.mapTo(ArrayList()) { it.tree(serializer) }

      newItems.forEach { item ->
        val itemHistory = item.history(serializer)
        itemHistory.forEach { historyChange += previousNodes + it }
        previousNodes += itemHistory.last()
      }

      onPush(historyChange)
    }

    oldKeys.startsWith(newKeys) -> { // Items popped
      console.log("WebHistory | when items popped")
      val oldPaths =
          oldHistory.takeLast(oldHistory.size - newHistory.size).flatMap { it.history(serializer) }
      onPop(-oldPaths.size)
    }

    else -> { // Rewriting the history
      console.log("WebHistory | when rewriting the history")
      val historyChange = ArrayList<List<Node<T>>>()
      val previousNodes = ArrayList<Node<T>>()

      newHistory.forEach { item ->
        val itemHistory = item.history(serializer)
        itemHistory.forEach { historyChange += previousNodes + it }
        previousNodes += itemHistory.last()
      }

      val oldPaths = oldHistory.flatMap { it.history(serializer) }

      onRewrite(oldPaths.size, historyChange)
    }
  }
}

private fun <T : HistoryItem.Data> HistoryItem<T>.history(
    serializer: KSerializer<T>
): List<Node<T>> {
  if (child == null) {
    return listOf(Node(data = data, serializer = serializer, children = emptyList()))
  }

  return child.webHistoryNavigation.history().map {
    Node(data = data, serializer = serializer, children = it)
  }
}

private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.history(): List<List<Node<*>>> {
  val nodes = ArrayList<List<Node<*>>>()
  val historyNodes = ArrayList<Node<*>>()
  history.value.forEach { item ->
    item.history(serializer).forEach { node ->
      historyNodes += node
      nodes += historyNodes.toList()
    }
  }

  return nodes
}

private fun <T : HistoryItem.Data> HistoryItem<T>.tree(serializer: KSerializer<T>): Node<T> =
    Node(
        data = data,
        serializer = serializer,
        children = child?.webHistoryNavigation?.tree() ?: emptyList(),
    )

private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.tree(): List<Node<T>> =
    history.value.map { it.tree(serializer) }

private fun Node<*>.url(): String {
  val path = path()
  val parameters = parameters()

  return when {
    path.isNotEmpty() && parameters.isNotEmpty() -> "$path?$parameters"
    path.isNotEmpty() -> path
    parameters.isNotEmpty() -> parameters
    else -> ""
  }
}

private fun Node<*>.path(): String {
  val segments = ArrayList<String>()
  collectPath(segments)

  return segments.joinToString(separator = "/")
}

private fun Node<*>.collectPath(segments: MutableList<String>) {
  if (data.path.isNotEmpty()) {
    segments += data.path
  }

  children.lastOrNull()?.collectPath(segments)
}

private fun Node<*>.parameters(): String {
  val parameters = LinkedHashMap<String, String>()
  collectParameters(parameters)

  return parameters.entries.joinToString(separator = "&") { (name, value) -> "$name=$value" }
}

private fun Node<*>.collectParameters(parameters: MutableMap<String, String>) {
  parameters += data.parameters
  children.lastOrNull()?.collectParameters(parameters)
}

private fun SerializableContainer.serializeToString(): String =
    Json.encodeToString(SerializableContainer.serializer(), this)

private fun String.deserializeContainer(): SerializableContainer =
    Json.decodeFromString(SerializableContainer.serializer(), this)

// private fun <T : HistoryItem.Data> WebHistoryNavigation<T>.saveHistory(): SerializableContainer =
//    SerializableContainer(
//        value = history.value.map { it.toNode() },
//        strategy = nodeListSerializer(serializer),
//    )

// private fun <T : HistoryItem.Data> HistoryItem<T>.toNode(): Node<T> =
//    Node(
//        data = data,
//        children = child?.webHistoryNavigation?.history?.value.map { it.toNode() } ?: emptyList(),
////        children = child?.webHistoryNavigation?.saveHistory(),
//    )

// private fun <T : HistoryItem.Data> SerializableContainer.restoreHistory(
//    serializer: KSerializer<T>,
// ): List<Node<T>> =
//    consumeRequired(strategy = nodeListSerializer(serializer))
//
// private fun <T : HistoryItem.Data> nodeListSerializer(serializer: KSerializer<T>):
// KSerializer<List<Node<T>>> =
//    ListSerializer(Node.serializer(serializer))

@Serializable
private data class SerializableNode(
    val data: SerializableContainer,
    val children: List<SerializableNode>,
)

private data class Node<T : HistoryItem.Data>(
    val data: T,
    val serializer: KSerializer<T>,
    val children: List<Node<*>>,
)

private inline fun subscription(block: SubscriptionScope.() -> Cancellation): Subscription {
  val scope = SubscriptionScopeImpl()

  return SubscriptionImpl(scope, scope.block())
}

private interface SubscriptionScope {
  var isEnabled: Boolean
}

private class SubscriptionScopeImpl : SubscriptionScope {
  override var isEnabled: Boolean = true
}

private interface Subscription : SubscriptionScope, Cancellation

private class SubscriptionImpl(
    scope: SubscriptionScope,
    cancellation: Cancellation,
) : Subscription, SubscriptionScope by scope, Cancellation by cancellation
