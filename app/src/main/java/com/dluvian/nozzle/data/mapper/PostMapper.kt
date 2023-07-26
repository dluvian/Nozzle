package com.dluvian.nozzle.data.mapper

import com.dluvian.nozzle.data.provider.IInteractionStatsProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.SHORT_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDebounce
import com.dluvian.nozzle.model.InteractionStats
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.NameAndPicture
import com.dluvian.nozzle.model.NameAndPubkey
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class PostMapper(
    private val interactionStatsProvider: IInteractionStatsProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val postDao: PostDao,
    private val profileDao: ProfileDao,
    private val eventRelayDao: EventRelayDao,
    private val contactDao: ContactDao,
) : IPostMapper {

    override suspend fun mapToPostsWithMetaFlow(posts: List<PostEntity>): Flow<List<PostWithMeta>> {
        if (posts.isEmpty()) return flow { emit(emptyList()) }

        val postIds = posts.map { it.id }
        val pubkeys = posts.map { it.pubkey }

        val statsFlow = interactionStatsProvider.getStatsFlow(postIds)
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val namesAndPicturesFlow = profileDao.getNamesAndPicturesMapFlow(pubkeys)
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val replyRecipientsFlow = profileDao.getAuthorNamesAndPubkeysMapFlow(
            postIds = posts.mapNotNull { it.replyToId }
        ).firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val mentionedPostsFlow: Flow<Map<String, MentionedPost>> = TODO("getMentionedPosts")
        // TODO: Use contactlistProvider
        val contactPubkeysFlow = contactDao.listContactPubkeysFlow(pubkeyProvider.getPubkey())
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val relaysFlow = eventRelayDao.getRelaysPerEventIdMapFlow(postIds)
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val trustScorePerPubkeyFlow = contactDao.getTrustScorePerPubkeyFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkeys = pubkeys
        ).firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()

        val baseFlow = getBaseFlow(
            posts = posts,
            statsFlow = statsFlow,
            namesAndPicturesFlow = namesAndPicturesFlow,
            contactPubkeysFlow = contactPubkeysFlow,
            replyRecipientsFlow = replyRecipientsFlow,
            relaysFlow = relaysFlow
        ).firstThenDebounce(SHORT_DEBOUNCE)
            .distinctUntilChanged()

        return combine(
            baseFlow,
            mentionedPostsFlow,
            trustScorePerPubkeyFlow
        ) { base, mentionedPosts, trustScore ->
            base.map {
                it.copy(
                    mentionedPosts = it.mentionedPosts.map { it.id }
                        .let { mentionedIds ->
                            mentionedPosts.filter { mentionedIds.contains(it.key) }.values
                        }
                        .toList(),
                    trustScore = if (isOneself(it.pubkey)) null
                    else trustScore[it.pubkey]
                )
            }
        }
    }

    private fun getBaseFlow(
        posts: List<PostEntity>,
        statsFlow: Flow<InteractionStats>,
        namesAndPicturesFlow: Flow<Map<String, NameAndPicture>>,
        contactPubkeysFlow: Flow<List<String>>,
        replyRecipientsFlow: Flow<Map<String, NameAndPubkey>>,
        relaysFlow: Flow<Map<String, List<String>>>
    ): Flow<List<PostWithMeta>> {
        return combine(
            statsFlow,
            namesAndPicturesFlow,
            contactPubkeysFlow,
            replyRecipientsFlow,
            relaysFlow
        ) { stats, namesAndPics, contacts, replyRecipients, relays ->
            posts.map {
                PostWithMeta(
                    id = it.id,
                    replyToId = it.replyToId,
                    replyToRootId = it.replyToRootId,
                    replyToName = replyRecipients[it.replyToId]?.name,
                    replyToPubkey = replyRecipients[it.replyToId]?.pubkey,
                    replyRelayHint = it.replyRelayHint,
                    pubkey = it.pubkey,
                    createdAt = it.createdAt,
                    content = it.content,
                    name = namesAndPics[it.pubkey]?.name.orEmpty(),
                    pictureUrl = namesAndPics[it.pubkey]?.picture.orEmpty(),
                    isLikedByMe = stats.isLikedByMe(it.id),
                    isFollowedByMe = if (isOneself(it.pubkey)) false
                    else contacts.contains(it.pubkey),
                    isOneself = isOneself(it.pubkey),
                    trustScore = if (isOneself(it.pubkey)) null else 0f,
                    numOfReplies = stats.getNumOfReplies(it.id),
                    relays = relays[it.id].orEmpty(),
                    mediaUrls = it.mediaUrls,
                    mentionedPosts = it.mentionedPostIds.map { mentionedId ->
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

    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()
}
