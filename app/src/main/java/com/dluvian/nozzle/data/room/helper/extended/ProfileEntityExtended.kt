package com.dluvian.nozzle.data.room.helper.extended

import androidx.room.Embedded
import com.dluvian.nozzle.data.room.entity.ProfileEntity

data class ProfileEntityExtended(
    @Embedded
    val profileEntity: ProfileEntity,
    val numOfFollowing: Int,
    val numOfFollowers: Int,
    val followsYou: Boolean,
)