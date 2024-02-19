package com.dluvian.nozzle.data.nip65Updater

import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.helper.Nip65Relay

class Nip65Updater(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao
) : INip65Updater {
    override suspend fun publishAndSaveInDb(nip65Relays: List<Nip65Relay>) {
        val event = nostrService.publishNip65(nip65Relays = nip65Relays)
        val entities = nip65Relays.map {
            Nip65Entity(
                nip65Relay = Nip65Relay(
                    url = it.url,
                    isRead = it.isRead,
                    isWrite = it.isWrite,
                ),
                pubkey = pubkeyProvider.getActivePubkey(),
                createdAt = event.createdAt
            )
        }
        nip65Dao.insertAndDeleteOutdated(nip65s = entities)
    }
}
