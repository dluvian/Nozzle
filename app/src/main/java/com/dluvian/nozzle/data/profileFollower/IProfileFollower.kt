package com.dluvian.nozzle.data.profileFollower

import androidx.compose.runtime.State
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.CoroutineScope

interface IProfileFollower {
    fun follow(scope: CoroutineScope, pubkeyToFollow: Pubkey)
    fun unfollow(scope: CoroutineScope, pubkeyToUnfollow: Pubkey)
    fun getIsFollowedByMeState(pubkey: Pubkey): State<Boolean>
}
