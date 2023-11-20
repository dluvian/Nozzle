package com.dluvian.nozzle.model

import com.dluvian.nozzle.model.nostr.Metadata

data class ProfileWithMeta(
    val pubkey: String,
    val nprofile: String,
    val metadata: Metadata,
    val numOfFollowing: Int,
    val numOfFollowers: Int,
    val relays: List<String>,
    val isOneself: Boolean,
    val trustScore: Float?,
) {
    companion object {
        fun createEmpty(pubkey: Pubkey = "") = ProfileWithMeta(
            pubkey = pubkey,
            nprofile = "",
            metadata = Metadata(),
            numOfFollowing = 0,
            numOfFollowers = 0,
            relays = emptyList(),
            isOneself = false,
            trustScore = null,
        )
    }
}
