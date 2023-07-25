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
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RepostPreview
import kotlinx.coroutines.FlowPreview
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

    @OptIn(FlowPreview::class)
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
        val repostsFlow = postDao.getRepostsPreviewMapFlow(posts.mapNotNull { it.repostedId })
            .emitThenDebounce(toEmit = emptyMap(), millis = NORMAL_DEBOUNCE)
            .distinctUntilChanged()
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
        ).emitThenDebounce(toEmit = emptyMap(), millis = NORMAL_DEBOUNCE)
            .distinctUntilChanged()

        val mainFlow = flow {
            emit(posts.map {
                PostWithMeta(
                    id = it.id,
                    replyToId = it.replyToId,
                    replyToRootId = it.replyToRootId,
                    replyToName = "",
                    replyToPubkey = "",
                    replyRelayHint = it.replyRelayHint,
                    pubkey = it.pubkey,
                    createdAt = it.createdAt,
                    content = it.content,
                    name = "",
                    pictureUrl = "",
                    repost = it.repostedId?.let { repostedId ->
                        RepostPreview(
                            id = repostedId,
                            pubkey = "",
                            content = "",
                            name = "",
                            picture = "",
                            createdAt = 0,
                        )
                    },
                    isLikedByMe = false,
                    isRepostedByMe = false,
                    isFollowedByMe = false,
                    isOneself = isOneself(it.pubkey),
                    trustScore = if (isOneself(it.pubkey)) null else 0f,
                    numOfReplies = 0,
                    relays = emptyList(),
                )
            })
        }

        val baseFlow = combine(
            mainFlow,
            statsFlow,
            namesAndPicturesFlow,
            contactPubkeysFlow,
            replyRecipientsFlow
        ) { main, stats, namesAndPics, contacts, replyRecipients ->
            main.map {
                it.copy(
                    isLikedByMe = stats.isLikedByMe(it.id),
                    isRepostedByMe = stats.isRepostedByMe(it.id),
                    numOfReplies = stats.getNumOfReplies(it.id),
                    pictureUrl = namesAndPics[it.pubkey]?.picture.orEmpty(),
                    name = namesAndPics[it.pubkey]?.name.orEmpty(),
                    replyToName = replyRecipients[it.replyToId]?.name,
                    replyToPubkey = replyRecipients[it.replyToId]?.pubkey,
                    isFollowedByMe = if (isOneself(it.pubkey)) false
                    else contacts.contains(it.pubkey),
                )
            }
        }.firstThenDebounce(SHORT_DEBOUNCE)
            .distinctUntilChanged()

        return combine(
            baseFlow,
            repostsFlow,
            relaysFlow,
            trustScorePerPubkeyFlow
        ) { base, reposts, relays, trustScore ->
            base.map {
                it.copy(
                    repost = it.repost?.id.let { repostedId -> reposts[repostedId] },
                    relays = relays[it.id].orEmpty(),
                    trustScore = if (isOneself(it.pubkey)) null
                    else trustScore[it.pubkey]
                )
            }
        }
    }

    private fun isOneself(pubkey: String) = pubkey == pubkeyProvider.getPubkey()
}
