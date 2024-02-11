package com.dluvian.nozzle.data.room.helper.extended

import androidx.room.Embedded
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.helper.FollowInfo

data class ProfileEntityExtended(
    @Embedded
    val profileEntity: ProfileEntity?,
    val followInfo: FollowInfo
)
