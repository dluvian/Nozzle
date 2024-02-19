package com.dluvian.nozzle.model.relay

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.data.room.entity.RelayProfileEntity

@Immutable
data class RelayProfile(
    val entity: RelayProfileEntity,
    val annotatedDescription: AnnotatedString?,
    val annotatedPubkey: AnnotatedString?,
    val annotatedPaymentsUrl: AnnotatedString?,
    val annotatedSoftware: AnnotatedString?,
)
