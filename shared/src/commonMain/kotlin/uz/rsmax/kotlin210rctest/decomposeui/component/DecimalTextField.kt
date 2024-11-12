package uz.rsmax.kotlin210rctest.decomposeui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import uz.rsmax.kotlin210rctest.getDecimalSeparator
import uz.rsmax.kotlin210rctest.toStringIgnoreZero

@Composable
fun DecimalTextField(
  value: Double,
  onValueChange: (Double) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable ((TextFieldLabelScope.() -> Unit))? = null,
  labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Default(),
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  shape: Shape = OutlinedTextFieldDefaults.shape,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
  val allowDigitsAndDecimalSeparater = remember {
    object : InputTransformation {
      private val decimalSeparator = getDecimalSeparator()
      override fun TextFieldBuffer.transformInput() {
        val seq = asCharSequence()
        val isAllowedChars = seq.all { it.isDigit() || it.toString() == decimalSeparator }
        val onlyOneDecimalSeparator = seq.count { it.toString() == decimalSeparator } <= 1

        if (!isAllowedChars || !onlyOneDecimalSeparator) {
          revertAllChanges()
        } else {
          if (length > 0) {
            filterUnnecessaryLeadingZeros()
            if (seq[0].toString() == decimalSeparator) {
              insert(0, "0")
            }
          }
        }
      }
    }
  }
  val appendZeroTransformation = remember {
    object : OutputTransformation {
      private val decimalSeparator = getDecimalSeparator()
      override fun TextFieldBuffer.transformOutput() {
        if (length > 0 && charAt(length - 1).toString() == decimalSeparator) {
          append("0")
        }
      }
    }
  }
  OutlinedTextField(
    state = rememberTextFieldStateAndUpdate(value, onValueChange),
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    labelPosition = labelPosition,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    supportingText = supportingText,
    isError = isError,
    inputTransformation = allowDigitsAndDecimalSeparater,
    outputTransformation = appendZeroTransformation,
    lineLimits = lineLimits,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,
    keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Decimal),
  )
}

@Composable
private fun rememberTextFieldStateAndUpdate(
  value: Double,
  onValueChange: (Double) -> Unit = {},
  onErrorInput: () -> Unit = {}
): TextFieldState {

  val decimalValidation = remember<DecimalValidation> { DecimalValidationImpl() }

  val state = rememberTextFieldState(value.toStringIgnoreZero())

  LaunchedEffect(value) {
    if (decimalValidation.execute(state.text.toString()) != value) {
      state.edit { replace(0, length, value.toStringIgnoreZero()) }
    }
  }
  LaunchedEffect(Unit) {
    snapshotFlow { state.text }
      .drop(1)
      .collectLatest {
        val textAsString = it.toString()
        val validDecimal = decimalValidation.execute(textAsString)
        if (validDecimal != null) {
          onValueChange(validDecimal)
        } else {
          onErrorInput()
        }
      }
  }

  return state
}

private const val ZERO = '0'

private fun TextFieldBuffer.filterUnnecessaryLeadingZeros() {
  val seq = asCharSequence()
  if (length > 1 && seq[0] == ZERO) {
    val nextZeroPos = seq.takeWhile { it == ZERO }.count()
    if (length > nextZeroPos) {
      delete(0, nextZeroPos)
    } else if (nextZeroPos > 1) {
      delete(0, 1)
    }
  }
}

interface BaseValidation<T, R> {
  fun execute(value: T): R
}

interface DecimalValidation : BaseValidation<String, Double?>

internal class DecimalValidationImpl : DecimalValidation {

  override fun execute(value: String): Double? {
    return try {
      value.trim().padStart(1, '0').toDouble()
    } catch (err: Throwable) {
      return null
    }
  }
}