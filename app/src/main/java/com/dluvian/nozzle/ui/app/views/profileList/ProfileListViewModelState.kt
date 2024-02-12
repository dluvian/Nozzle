package com.dluvian.nozzle.ui.app.views.profileList

import com.dluvian.nozzle.model.Pubkey

data class ProfileListViewModelState(
    val pubkey: Pubkey = "",
    val type: ProfileListType = ProfileListType.FOLLOWER_LIST,
    val isRefreshing: Boolean = false
)
