package com.dluvian.nozzle.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.nostr.nip11.Nip11Document

@Entity(tableName = "relayProfile")
data class RelayProfileEntity(
    @PrimaryKey(autoGenerate = false) val relayUrl: Relay,
    @Embedded val profile: Nip11Document
)
