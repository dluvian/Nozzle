package com.dluvian.nozzle.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dluvian.nozzle.model.nostr.Metadata

@Entity(tableName = "profile", indices = [Index(value = ["pubkey"])])
data class ProfileEntity(
    @PrimaryKey(autoGenerate = false) val pubkey: String,
    @Embedded val metadata: Metadata,
    val createdAt: Long,
)
