package uz.rsmax.kotlin210rctest.decompose

import androidx.compose.runtime.Immutable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Immutable
sealed interface LoadableData<out T : Any> {
  data object Idle : LoadableData<Nothing>

  data class Loaded<T : Any>(val data: T) : LoadableData<T>

  data class Error(val error: Throwable) : LoadableData<Nothing>

  companion object {

    fun <T : Any> LoadableData<T>.isLoadedAnd(predicate: T.() -> Boolean): Boolean {
      return this is Loaded && data.predicate()
    }

    fun <T : Any> LoadableData<T>.runIfLoaded(block: (T) -> Unit) {
      if (this is Loaded) block(data)
    }

    fun <T : Any> LoadableData<T>.edit(block: T.() -> T): LoadableData<T> {
      return if (this is Loaded) copy(data = block(data)) else this
    }

    @OptIn(ExperimentalContracts::class)
    fun <T : Any> LoadableData<T>.isLoaded(): Boolean {
      contract { returns(true) implies (this@isLoaded is Loaded<T>) }
      return this is Loaded<T>
    }
  }
}