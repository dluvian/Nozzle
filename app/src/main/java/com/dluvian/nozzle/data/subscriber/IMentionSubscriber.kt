package com.dluvian.nozzle.data.subscriber

import com.dluvian.nozzle.data.room.helper.BasePost
import com.dluvian.nozzle.model.helper.PubkeysAndAuthorPubkeys
import com.dluvian.nozzle.model.nostr.Nevent

interface IMentionSubscriber {
    suspend fun subscribeMentionedPosts(basePosts: Collection<BasePost>): List<Nevent>

    suspend fun subscribeMentionedProfiles(
        basePosts: Collection<BasePost>
    ): PubkeysAndAuthorPubkeys
}