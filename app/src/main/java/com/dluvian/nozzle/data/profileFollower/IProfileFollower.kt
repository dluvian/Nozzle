package com.dluvian.nozzle.data.profileFollower

import androidx.compose.runtime.State
import com.dluvian.nozzle.model.Pubkey

interface IProfileFollower {
    fun follow(pubkeyToFollow: Pubkey)
    fun unfollow(pubkeyToUnfollow: Pubkey)
    fun getForceFollowedState(): State<Map<Pubkey, Boolean>>
}
