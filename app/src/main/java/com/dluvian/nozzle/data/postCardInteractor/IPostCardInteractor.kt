package com.dluvian.nozzle.data.postCardInteractor

interface IPostCardInteractor {
    suspend fun like(postId: String, postPubkey: String, relays: Collection<String>?)
    suspend fun repost(
        postId: String,
        postPubkey: String,
        originUrl: String,
        relays: Collection<String>?
    )
}
