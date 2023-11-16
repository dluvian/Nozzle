package com.dluvian.nozzle.ui.app.views.profileList

import com.dluvian.nozzle.model.SimpleProfile

sealed class ProfileList(val profiles: List<SimpleProfile>)

class FollowerList(profiles: List<SimpleProfile>) : ProfileList(profiles = profiles)

class FollowedByList(profiles: List<SimpleProfile>) : ProfileList(profiles = profiles)
