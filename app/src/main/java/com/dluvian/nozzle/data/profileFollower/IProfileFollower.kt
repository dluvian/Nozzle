package com.dluvian.nozzle.data.profileFollower

import com.dluvian.nozzle.model.Pubkey

interface IProfileFollower {
    fun follow(pubkeyToFollow: Pubkey)
    fun unfollow(pubkeyToUnfollow: Pubkey)
}
