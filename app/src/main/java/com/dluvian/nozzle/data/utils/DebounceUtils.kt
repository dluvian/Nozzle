package com.dluvian.nozzle.data.utils

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take

const val SHORT_DEBOUNCE = 100L
const val NORMAL_DEBOUNCE = 300L

// TODO: distinctFirstThenDebounce
@OptIn(FlowPreview::class)
fun <T> Flow<T>.firstThenDebounce(millis: Long): Flow<T> {
    return flow {
        emitAll(this@firstThenDebounce.take(1))
        emitAll(this@firstThenDebounce.drop(1).debounce(millis))
    }
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.emitThenDebounce(toEmit: T, millis: Long): Flow<T> {
    return flow {
        emit(toEmit)
        emitAll(this@emitThenDebounce.debounce(millis))
    }
}
