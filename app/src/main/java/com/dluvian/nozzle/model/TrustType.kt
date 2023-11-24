package com.dluvian.nozzle.model


sealed class TrustType {
    companion object {
        fun determineTrustType(
            isOneself: Boolean,
            isFollowed: Boolean,
            trustScore: Float?
        ): TrustType {
            return if (isOneself) Oneself
            else if (isFollowed) Friend
            else if (trustScore?.let { it > 0f && it <= 1f } == true) {
                FollowedByFriend(trustScore = trustScore)
            } else Unknown
        }
    }
}

data object Oneself : TrustType()

data object Unknown : TrustType()

data object Friend : TrustType()

class FollowedByFriend(val trustScore: Float) : TrustType()

