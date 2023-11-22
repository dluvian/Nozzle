package com.dluvian.nozzle.data.paginator

import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.StateFlow

interface IPaginator {
    fun getFeed(): StateFlow<StateFlow<List<PostWithMeta>>>
    fun loadMore()
    fun reset()
    fun refresh()
}
