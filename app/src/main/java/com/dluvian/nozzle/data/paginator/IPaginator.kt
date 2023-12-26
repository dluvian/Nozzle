package com.dluvian.nozzle.data.paginator

import kotlinx.coroutines.flow.StateFlow

interface IPaginator<T, S> {
    fun getList(): StateFlow<StateFlow<List<T>>>
    fun loadMore()
    fun refresh(waitForSubscription: Boolean, useInitialValue: Boolean)
}
