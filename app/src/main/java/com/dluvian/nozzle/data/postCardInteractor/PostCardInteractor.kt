package com.dluvian.nozzle.data.postCardInteractor

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.entity.PostEntity

private const val TAG = "PostCardInteractor"

class PostCardInteractor(
    private val nostrService: INostrService,
    private val reactionDao: ReactionDao,
    private val postDao: PostDao,
) : IPostCardInteractor {

    override suspend fun like(postId: String, postPubkey: String, relays: Collection<String>?) {
        Log.i(TAG, "Like $postId")
        val event = nostrService.sendLike(
            postId = postId,
            postPubkey = postPubkey,
            relays = relays
        )
        reactionDao.like(pubkey = event.pubkey, eventId = postId)
    }

    override suspend fun repost(
        postId: String,
        postPubkey: String,
        originUrl: String,
        relays: Collection<String>?
    ) {
        Log.i(TAG, "Repost $postId")
        // TODO: Quote repost
        val event = nostrService.sendRepost(
            postId = postId,
            postPubkey = postPubkey,
            quote = "",
            originUrl = originUrl,
            relays = relays
        )
        postDao.insertIfNotPresent(PostEntity.fromEvent(event))
    }
}
