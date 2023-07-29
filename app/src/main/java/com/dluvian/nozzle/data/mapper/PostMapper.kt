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
import com.dluvian.nozzle.data.utils.emitThenDebounce
import com.dluvian.nozzle.data.utils.firstThenDebounce
import com.dluvian.nozzle.model.InteractionStats
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.NameAndPicture
import com.dluvian.nozzle.model.NameAndPubkey
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
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
            .distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)
        val namesAndPicturesFlow = profileDao.getNamesAndPicturesMapFlow(pubkeys)
            .distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)
        val replyRecipientsFlow = profileDao.getAuthorNamesAndPubkeysMapFlow(
            postIds = posts.mapNotNull { it.replyToId }
        ).distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)

        // TODO: Use contactlistProvider
        val contactPubkeysFlow = contactDao.listContactPubkeysFlow(pubkeyProvider.getPubkey())
            .distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)

        val relaysFlow = eventRelayDao.getRelaysPerEventIdMapFlow(postIds)
            .distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)
        val trustScorePerPubkeyFlow = contactDao.getTrustScorePerPubkeyFlow(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkeys = pubkeys
        ).distinctUntilChanged()
            .firstThenDebounce(NORMAL_DEBOUNCE)

        val mentionedPostIds = posts.mapNotNull { it.mentionedPostId }
        val mentionedPostsFlow = if (mentionedPostIds.isEmpty()) emptyFlow()
        else postDao.getMentionedPostsMapFlow(postIds = mentionedPostIds)
            .distinctUntilChanged()
            .emitThenDebounce(toEmit = emptyMap(), millis = NORMAL_DEBOUNCE)

        val baseFlow = getBaseFlow(
            posts = posts,
            statsFlow = statsFlow,
            namesAndPicturesFlow = namesAndPicturesFlow,
            contactPubkeysFlow = contactPubkeysFlow,
            replyRecipientsFlow = replyRecipientsFlow,
            relaysFlow = relaysFlow
        ).distinctUntilChanged()
            .firstThenDebounce(SHORT_DEBOUNCE)

        return combine(
            baseFlow,
            mentionedPostsFlow,
            trustScorePerPubkeyFlow
        ) { base, mentionedPosts, trustScore ->
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
                    mediaUrl = it.mediaUrl,
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

    // Move to pubkey provider
    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()
}
