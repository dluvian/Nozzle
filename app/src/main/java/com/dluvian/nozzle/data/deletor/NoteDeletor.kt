package com.dluvian.nozzle.data.deletor

import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.model.NoteId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDeletor(
    private val nostrService: INostrService,
    private val dbExcludingCache: IIdCache,
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
) : INoteDeletor {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun deleteNote(noteId: NoteId) {
        dbExcludingCache.removePostId(noteId = noteId)
        scope.launch {
            postDao.deletePost(postId = noteId)
            nostrService.deleteEvent(
                eventId = noteId,
                seenInRelays = eventRelayDao.listSeenInRelays(eventId = noteId)
            )
        }
    }
}
