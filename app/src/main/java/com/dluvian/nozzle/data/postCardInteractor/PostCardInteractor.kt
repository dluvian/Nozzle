package com.dluvian.nozzle.data.postCardInteractor

import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.entity.ReactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PostCardInteractor(
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val reactionDao: ReactionDao,
) : IPostCardInteractor {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun like(postId: String, postPubkey: String) {
        scope.launch(context = Dispatchers.IO) {
            val event = nostrService.sendLike(
                postId = postId,
                postPubkey = postPubkey,
                relays = relayProvider.getReadRelaysOfPubkey(postPubkey).toSet()
                        + relayProvider.getWriteRelays()
            )
            reactionDao.insertOrIgnore(ReactionEntity(eventId = postId, pubkey = event.pubkey))
        }
    }
}
