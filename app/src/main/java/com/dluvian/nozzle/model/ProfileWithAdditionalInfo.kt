package com.dluvian.nozzle.model

import com.dluvian.nostrclientkt.model.Metadata

data class ProfileWithAdditionalInfo(
    val pubkey: String,
    val npub: String,
    val metadata: Metadata,
    val numOfFollowing: Int,
    val numOfFollowers: Int,
    val relays: List<String>,
    val isOneself: Boolean,
    val isFollowedByMe: Boolean,
)
