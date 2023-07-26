package com.dluvian.nozzle.data.postCardInteractor

interface IPostCardInteractor {
    suspend fun like(postId: String, postPubkey: String, relays: Collection<String>?)
}
