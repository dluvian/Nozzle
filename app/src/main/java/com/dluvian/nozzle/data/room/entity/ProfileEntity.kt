package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dluvian.nozzle.data.nostr.client.model.Metadata

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = false) val pubkey: String,
    val name: String,
    val about: String,
    val picture: String,
    val nip05: String,
    val lud16: String,
    val createdAt: Long,
) {
    fun getMetadata(): Metadata {
        return Metadata(
            name = name,
            about = about,
            picture = picture,
            nip05 = nip05,
            lud16 = lud16
        )
    }
}
