package com.dluvian.nozzle.model

sealed class TrustType

object Oneself : TrustType()

object Unknown : TrustType()

object Friend : TrustType()

class FollowedByFriend(val percentageOfFriends: Float) : TrustType()

fun determineTrustType(
    isOneself: Boolean,
    isFollowed: Boolean,
    followedByFriendsPercentage: Float?
): TrustType {
    return if (isOneself) Oneself
    else if (isFollowed) Friend
    else if (followedByFriendsPercentage?.let { it > 0f && it <= 1f } == true) {
        FollowedByFriend(percentageOfFriends = followedByFriendsPercentage)
    } else Unknown
}