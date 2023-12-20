package com.dluvian.nozzle.data.postCardInteractor

import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.dao.RepostDao
import com.dluvian.nozzle.data.room.entity.ReactionEntity
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PostCardInteractor(
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val reactionDao: ReactionDao,
    private val repostDao: RepostDao,
) : IPostCardInteractor {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun like(noteId: NoteId, postPubkey: Pubkey) {
        scope.launch(context = Dispatchers.IO) {
            val event = nostrService.sendLike(
                postId = noteId,
                postPubkey = postPubkey,
                isRepost = repostDao.isRepost(eventId = noteId),
                relays = relayProvider.getReadRelaysOfPubkey(postPubkey).toSet()
                        + relayProvider.getWriteRelays()
            )

            reactionDao.insertOrIgnore(ReactionEntity(eventId = noteId, pubkey = event.pubkey))
        }
    }

    override fun deleteLike(noteId: NoteId) {
        scope.launch {
            reactionDao.deleteReaction(eventId = noteId)
            nostrService.deleteEvent(eventId = noteId)
        }
    }
}
