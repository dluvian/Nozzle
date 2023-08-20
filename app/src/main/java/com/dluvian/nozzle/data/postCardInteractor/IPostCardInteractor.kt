package com.dluvian.nozzle.data.postCardInteractor

import kotlinx.coroutines.CoroutineScope

interface IPostCardInteractor {
    fun like(scope: CoroutineScope, postId: String, postPubkey: String)
}
