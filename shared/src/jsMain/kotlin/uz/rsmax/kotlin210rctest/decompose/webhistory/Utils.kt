package com.arkivanov.decompose.router.stack

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value

internal fun <T : Any> Value<T>.subscribe(
    skipInitial: Boolean = false,
    observer: (new: T, old: T) -> Unit,
): Cancellation {
    var old = value
    var isInitial = true

    return subscribe callback@{ new ->
        val tmp = old
        old = new

        if (isInitial) {
            isInitial = false
            if (skipInitial) {
                return@callback
            }
        }

        observer(new, tmp)
    }
}