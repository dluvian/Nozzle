package com.dluvian.nozzle.data.postCardInteractor

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.room.dao.ReactionDao

private const val TAG = "PostCardInteractor"

class PostCardInteractor(
    private val nostrService: INostrService,
    private val reactionDao: ReactionDao,
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
}
