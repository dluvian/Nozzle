package com.dluvian.nozzle.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class ListAndNumberFlow<T>(
    val listFlow: Flow<List<T>> = flowOf(emptyList()),
    val numFlow: Flow<Int> = flowOf(0),
)
