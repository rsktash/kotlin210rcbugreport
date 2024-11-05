package uz.rsmax.kotlin210rctest

import uz.rsmax.base.EditableData
import uz.rsmax.base.EditableData.Companion.edit
import uz.rsmax.kotlin210rctest.Test2_1_0_RC_Store.State
import kotlin.js.JsName

fun Test210RCComponent(): Test2_1_0_RC_Component = Test2_1_0_RC_ComponentImpl()

abstract class Test2_1_0_RC_Component {

  abstract fun onDisposeChanges()

  abstract fun onSave()
}

interface Test2_1_0_RC_Store {

  data class State
  internal constructor(
    val canNavigateBack: Boolean,
    val isNew: Boolean,
    val address: EditableData<EditData> = EditableData.Idle,
  )
}

internal class Test2_1_0_RC_ComponentImpl : Test2_1_0_RC_Component() {

  override fun onDisposeChanges() {
    TODO("Not yet implemented")
  }

  override fun onSave() {
    TODO("Not yet implemented")
  }

  private sealed interface Message {

    data object OnSwitchPromptDisposeChanges : Message
  }

  private object ReducerImpl : Reducer<State, Message> {
    override fun State.reduce(msg: Message): State =
      when (msg) {
        is Message.OnSwitchPromptDisposeChanges -> {
          copy(address = address.edit { copy(promtDisposeChanges = !promtDisposeChanges) })
        }
      }
  }
}


data class EditData(
  val name: String = "",
  val nameIsValid: Boolean = true,
  val isDeleted: Boolean = false,
)

fun interface Reducer<State, in Message> {

  @JsName("reduce")
  fun State.reduce(msg: Message): State
}