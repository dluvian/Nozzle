package com.dluvian.nozzle.data.mapper

import android.util.Log
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.PostEntityExtended
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class PostMapper(
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
    private val contactDao: ContactDao,
) : IPostMapper {

    // TODO: Simplify with SQL joins

    override suspend fun mapToPostsWithMetaFlow(posts: List<PostEntity>): Flow<List<PostWithMeta>> {
        if (posts.isEmpty()) return flow { emit(emptyList()) }

        val postIds = posts.map { it.id }
        val pubkeys = posts.map { it.pubkey }

        val extendedPosts = postDao.listExtendedPostsFlow(
            postIds = postIds,
            personalPubkey = pubkeyProvider.getPubkey()
        )

        val contactPubkeysFlow = contactListProvider.getPersonalContactPubkeysFlow()

        val relaysFlow = eventRelayDao.getRelaysPerEventIdMapFlow(postIds)


        // Short debounce because of immediate user interaction
        val trustScorePerPubkeyFlow = contactDao.getTrustScorePerPubkeyFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkeys = pubkeys
        )

        // TODO: In intitial SQL Query
        val mentionedPostIds = posts.mapNotNull { it.mentionedPostId }
        val mentionedPostsFlow =
            if (mentionedPostIds.isEmpty()) flow { emit(emptyMap()) }
            else postDao.getMentionedPostsMapFlow(postIds = mentionedPostIds)

        val baseFlow = getBaseFlow(
            posts = posts,
            extendedPostsFlow = extendedPosts,
            contactPubkeysFlow = contactPubkeysFlow,
            relaysFlow = relaysFlow
        )

        return combine(
            baseFlow,
            mentionedPostsFlow,
            trustScorePerPubkeyFlow,
        ) { base, mentionedPosts, trustScore ->
            Log.i("LOLOL", "2")

            base.map {
                it.copy(
                    mentionedPost = it.mentionedPost?.id?.let { mentionedPostId ->
                        mentionedPosts[mentionedPostId]
                    },
                    trustScore = if (isOneself(it.pubkey)) null
                    else trustScore[it.pubkey]
                )
            }
        }
    }

    private fun getBaseFlow(
        posts: List<PostEntity>,
        extendedPostsFlow: Flow<List<PostEntityExtended>>,
        contactPubkeysFlow: Flow<List<String>>,
        relaysFlow: Flow<Map<String, List<String>>>
    ): Flow<List<PostWithMeta>> {
        return combine(
            extendedPostsFlow,
            contactPubkeysFlow,
            relaysFlow
        ) { extended, contacts, relays ->
            Log.i("LOLOL", "1")
            posts.map {
                val extendedPost = extended.find { ext -> ext.postEntity.id == it.id }
                PostWithMeta(
                    id = it.id,
                    replyToId = it.replyToId,
                    replyToName = extendedPost?.replyToName,
                    replyToPubkey = extendedPost?.replyToPubkey,
                    replyRelayHint = it.replyRelayHint,
                    pubkey = it.pubkey,
                    createdAt = it.createdAt,
                    content = it.content,
                    name = extendedPost?.name.orEmpty(),
                    pictureUrl = extendedPost?.pictureUrl.orEmpty(),
                    isLikedByMe = extendedPost?.isLikedByMe ?: false,
                    isFollowedByMe = if (isOneself(it.pubkey)) false
                    else contacts.contains(it.pubkey),
                    isOneself = isOneself(it.pubkey),
                    trustScore = if (isOneself(it.pubkey)) null else 0f,
                    numOfReplies = extendedPost?.numOfReplies ?: 0,
                    relays = relays[it.id].orEmpty(),
                    mentionedPost = it.mentionedPostId?.let { mentionedId ->
                        MentionedPost(
                            id = mentionedId,
                            pubkey = "",
                            content = "",
                            name = "",
                            picture = "",
                            createdAt = 0,
                        )
                    },
                )
            }
        }
    }

    // TODO: Move to pubkey provider
    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()
}
