package com.dluvian.nozzle.model

import com.dluvian.nozzle.model.nostr.Metadata

data class ProfileWithAdditionalInfo(
    val pubkey: String,
    val npub: String,
    val metadata: Metadata,
    val numOfFollowing: Int,
    val numOfFollowers: Int,
    val relays: List<String>,
    val isOneself: Boolean,
    val isFollowedByMe: Boolean,
    val trustScore: Float?,
) {
    companion object {
        fun createEmpty() = ProfileWithAdditionalInfo(
            pubkey = "",
            npub = "",
            metadata = Metadata(),
            numOfFollowing = 0,
            numOfFollowers = 0,
            relays = emptyList(),
            isOneself = false,
            isFollowedByMe = false,
            trustScore = null,
        )
    }
}
