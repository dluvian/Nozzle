package com.dluvian.nozzle.data.postPreparer

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.removeMentionCharOrNostrUri
import com.dluvian.nozzle.data.nostr.utils.MentionUtils
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.PostWithTagsAndMentions
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId

class PostPreparer(
    private val simpleProfileProvider: ISimpleProfileProvider,
    private val relayProvider: IRelayProvider
) : IPostPreparer {
    override suspend fun getCleanPostWithTagsAndMentions(content: String): PostWithTagsAndMentions {
        val strBuilder = StringBuilder(content.trim())
        val allMentions = MentionUtils.extractMentionedProfiles(content)
        allMentions
            .sortedByDescending { it.range.first }
            .forEach {
                strBuilder.replace(
                    it.range.first,
                    it.range.last + 1,
                    getImprovedMention(it.value)
                )
            }
        val finalContent = strBuilder.toString()
        return PostWithTagsAndMentions(
            content = finalContent,
            hashtags = HashtagUtils.extractHashtagValues(finalContent).distinct(),
            mentions = allMentions
                .mapNotNull { EncodingUtils.profileIdToNostrId(it.value.removeMentionCharOrNostrUri()) }
                .map { it.hex }
                .toList()
                .distinct()
        )
    }

    override suspend fun searchProfiles(nameLike: String, limit: Int): List<SimpleProfile> {
        return simpleProfileProvider.getSimpleProfiles(nameLike, limit)
    }

    private suspend fun getImprovedMention(mention: String): String {
        val withoutPrefix = mention.removeMentionCharOrNostrUri()
        val nostrId = EncodingUtils.profileIdToNostrId(profileId = withoutPrefix) ?: return mention

        val improvedMention = when (nostrId) {
            is NpubNostrId -> {
                val writeRelays = relayProvider.getWriteRelaysOfPubkey(nostrId.pubkeyHex)
                EncodingUtils.createNprofileStr(pubkey = nostrId.pubkeyHex, relays = writeRelays)
            }

            is NprofileNostrId -> withoutPrefix
            else -> withoutPrefix
        } ?: withoutPrefix

        return EncodingUtils.URI + improvedMention
    }
}
