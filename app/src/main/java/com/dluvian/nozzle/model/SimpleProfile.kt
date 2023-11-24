package com.dluvian.nozzle.model

data class SimpleProfile(
    val name: String,
    val picture: PictureUrl,
    val pubkey: Pubkey,
    val trustScore: Float,
    val isOneself: Boolean,
    val isFollowedByMe: Boolean
) : Identifiable {
    override fun getId() = pubkey
}
