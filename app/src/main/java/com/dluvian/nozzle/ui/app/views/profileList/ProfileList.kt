package com.dluvian.nozzle.ui.app.views.profileList

import com.dluvian.nozzle.model.SimpleProfile

data class ProfileList(
    val profiles: List<SimpleProfile> = emptyList(),
    val type: ProfileListType = ProfileListType.FOLLOWER_LIST
)