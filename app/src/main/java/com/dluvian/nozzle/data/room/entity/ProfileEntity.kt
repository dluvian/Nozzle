package com.dluvian.nozzle.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.nostr.Metadata

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = false) val pubkey: String,
    @Embedded val metadata: Metadata,
    val createdAt: Long,
)
