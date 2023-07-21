package com.dluvian.nozzle.model

sealed class TrustType

object Oneself : TrustType()

object Unknown : TrustType()

object Friend : TrustType()

class FollowedByFriend(val trustScore: Float) : TrustType()

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