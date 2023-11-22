package com.dluvian.nozzle.data.paginator

import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.PostWithMeta
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

class Paginator(
    private val scope: CoroutineScope,
    private val onSetRefreshing: (Boolean) -> Unit,
    private val onGetPage: suspend (Long) -> Flow<List<PostWithMeta>>
) : IPaginator {
    private val maxPageSize = 4
    private var pages: MutableList<StateFlow<List<PostWithMeta>>> = mutableListOf()
    private val feed: MutableStateFlow<StateFlow<List<PostWithMeta>>> =
        MutableStateFlow(MutableStateFlow(emptyList()))


    override fun getFeed() = feed

    private val isLoadingMore = AtomicBoolean(false)

    override fun loadMore() {
        if (!isLoadingMore.compareAndSet(false, true)) return

        onSetRefreshing(true)
        scope.launch(context = Dispatchers.IO) {
            val lastItem = feed.value.value.lastOrNull() ?: return@launch
            val newPage = onGetPage(lastItem.entity.createdAt).stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                initialValue = emptyList(),
            )
            if (pages.size >= maxPageSize) pages.removeFirst()
            pages.add(newPage)
            feed.update { createNewFeed(pages = pages, initialValue = feed.value.value) }
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
            val newPage = onGetPage(getCurrentTimeInSeconds()).stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                initialValue = initialValue,
            )
            pages.add(newPage)
            feed.update { createNewFeed(pages = pages, initialValue = initialValue) }
            delay(WAIT_TIME)
        }.invokeOnCompletion {
            onSetRefreshing(false)
        }
    }

    private fun createNewFeed(
        pages: MutableList<StateFlow<List<PostWithMeta>>>,
        initialValue: List<PostWithMeta>,
    ): StateFlow<List<PostWithMeta>> {
        return combine(
            pages.getOrElse(pages.size - 4) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 3) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 2) { flowOf(emptyList()) },
            pages.getOrElse(pages.size - 1) { flowOf(emptyList()) }
        ) { p1, p2, p3, p4 -> p1 + p2 + p3 + p4 }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                initialValue = initialValue
            )
    }
}
