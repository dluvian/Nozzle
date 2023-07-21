package com.dluvian.nozzle.model


sealed class TrustType {
    companion object {
        private val verifiedPubkeys = listOf(
            // Nozzle developer dluvian
            "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10"
        )

        fun determineTrustType(
            pubkey: String,
            isOneself: Boolean,
            isFollowed: Boolean,
            trustScore: Float?
        ): TrustType {
            val isVerified = verifiedPubkeys.contains(pubkey)
            return if (isOneself) Oneself
            else if (isFollowed) Friend(isVerified = isVerified)
            else if (trustScore?.let { it > 0f && it <= 1f } == true) {
                FollowedByFriend(trustScore = trustScore, isVerified = isVerified)
            } else Unknown(isVerified = isVerified)
        }
    }
}

object Oneself : TrustType()

class Unknown(val isVerified: Boolean = false) : TrustType()

class Friend(val isVerified: Boolean = false) : TrustType()

class FollowedByFriend(val trustScore: Float, val isVerified: Boolean = false) : TrustType()

