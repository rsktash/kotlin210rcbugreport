package uz.rsmax.base

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface EditableData<out T : Any> {
  data object Idle : EditableData<Nothing>

  data class Loaded<T : Any>(
      val data: T,
      val copy: T = data,
      val promtDisposeChanges: Boolean = false,
  ) : EditableData<T> {
    val isDirty: Boolean get() = data != copy
  }

  data class Error(val error: Throwable) : EditableData<Nothing>

  val loadedAndIsDirty: Boolean
    get() = this is Loaded && data != copy

  val loadedAndPromptDisposeChanges: Boolean
    get() = this is Loaded && this.promtDisposeChanges

  companion object {

    fun <T : Any> EditableData<T>.isLoadedAnd(predicate: Loaded<T>.() -> Boolean): Boolean {
      return this is Loaded && predicate()
    }

    fun <T : Any> EditableData<T>.runIfLoaded(block: (T) -> Unit) {
      if (this is Loaded) block(data)
    }
    fun <T : Any> EditableData<T>.edit(block: Loaded<T>.() -> Loaded<T>): EditableData<T> {
      return if (this is Loaded) this.block() else this
    }

    fun <T : Any> EditableData<T>.editData(block: T.() -> T): EditableData<T> {
      return if (this is Loaded) copy(data = block(data)) else this
    }

    @OptIn(ExperimentalContracts::class)
    fun <T : Any> EditableData<T>.isLoaded(): Boolean {
      contract { returns(true) implies (this@isLoaded is Loaded<T>) }
      return this is Loaded<T>
    }
  }
}


