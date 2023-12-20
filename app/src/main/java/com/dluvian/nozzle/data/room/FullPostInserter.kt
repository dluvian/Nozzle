package com.dluvian.nozzle.data.room

import com.dluvian.nozzle.data.room.dao.HashtagDao
import com.dluvian.nozzle.data.room.dao.MentionDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.RepostDao
import com.dluvian.nozzle.model.nostr.Event

class FullPostInserter(
    private val postDao: PostDao,
    private val hashtagDao: HashtagDao,
    private val mentionDao: MentionDao,
    private val repostDao: RepostDao
) {
    suspend fun insertFullPost(events: Collection<Event>) {
        postDao.insertWithHashtagsAndMentions(
            events = events,
            hashtagDao = hashtagDao,
            mentionDao = mentionDao,
        )
    }

    suspend fun insertReposts(events: Collection<Event>) {
        postDao.insertRepost(events = events, repostDao = repostDao)
    }
}
