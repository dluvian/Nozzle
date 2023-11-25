package com.dluvian.nozzle.data.paginator

import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.model.Identifiable
import com.dluvian.nozzle.model.WaitTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class Paginator<T : Identifiable, S>(
    private val scope: CoroutineScope,
    private val onSetRefreshing: (Boolean) -> Unit,
    private val onGetPage: suspend (S, WaitTime) -> Flow<List<T>>,
    private val onIdentifyLastParam: (T?) -> S,
) : IPaginator<T, S> {
    private val maxPageSize = 5
    private var pages: MutableList<StateFlow<List<T>>> = mutableListOf()
    private val list: MutableStateFlow<StateFlow<List<T>>> =
        MutableStateFlow(MutableStateFlow(emptyList()))

    override fun getList() = list

    private val isLoadingMore = AtomicBoolean(false)

    override fun loadMore() {
        if (!isLoadingMore.compareAndSet(false, true)) return

        onSetRefreshing(true)
        scope.launch(context = Dispatchers.IO) {
            val lastItem = list.value.value.lastOrNull() ?: return@launch
            val newPage = onGetPage(onIdentifyLastParam(lastItem), 0L).stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = SCOPE_TIMEOUT,
                    replayExpirationMillis = 0
                ),
                initialValue = emptyList(),
            )
            if (pages.size >= maxPageSize) pages.removeFirst()
            pages.add(newPage)
            list.update { createNewList(pages = pages, initialValue = list.value.value) }
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            isLoadingMore.set(false)
            onSetRefreshing(false)
        }
    }

    override fun reset() = resetOrRefresh(isRefresh = false)

    override fun refresh() = resetOrRefresh(isRefresh = true)

    private fun resetOrRefresh(isRefresh: Boolean) {
        val firstPage = pages.firstOrNull()?.value ?: emptyList()
        onSetRefreshing(true)
        val initialValue = if (isRefresh) firstPage else emptyList()
        pages.clear()
        scope.launch(context = Dispatchers.IO) {
            val waitForSubscription = if (isRefresh) WAIT_TIME else 0L
            val newPage = onGetPage(onIdentifyLastParam(null), waitForSubscription).stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = SCOPE_TIMEOUT,
                    replayExpirationMillis = 0
                ),
                initialValue = initialValue,
            )
            pages.add(newPage)
            list.update { createNewList(pages = pages, initialValue = initialValue) }
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            onSetRefreshing(false)
        }
    }

    private fun createNewList(
        pages: MutableList<StateFlow<List<T>>>,
        initialValue: List<T>,
    ): StateFlow<List<T>> {
        return combine(
            pages.getOrElse(pages.size - 5) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 4) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 3) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 2) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 1) { flowOf(emptyList()) }
        ) { p1, p2, p3, p4, p5 ->
            val list = initialValue + p1 + p2 + p3 + p4 + p5
            list.reversed()
                .distinctBy { it.getId() }
                .reversed()
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = SCOPE_TIMEOUT,
                    replayExpirationMillis = 0
                ),
                initialValue = initialValue
            )
    }
}
