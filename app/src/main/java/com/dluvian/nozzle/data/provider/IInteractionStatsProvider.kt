package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.InteractionStats
import kotlinx.coroutines.flow.Flow

interface IInteractionStatsProvider {
    fun getStatsFlow(postIds: List<String>): Flow<InteractionStats>
}
