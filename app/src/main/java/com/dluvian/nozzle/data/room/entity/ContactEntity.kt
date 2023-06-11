package com.dluvian.nozzle.data.room.entity

import androidx.room.Entity
import com.dluvian.nozzle.model.nostr.ContactListEntry

@Entity(tableName = "contact", primaryKeys = ["pubkey", "contactPubkey"])
data class ContactEntity(
    val pubkey: String,
    val contactPubkey: String,
    val relayUrl: String,
    val createdAt: Long,
) {
    fun toContactListEntry(): ContactListEntry {
        return ContactListEntry(pubkey = contactPubkey, relayUrl = relayUrl)
    }
}
