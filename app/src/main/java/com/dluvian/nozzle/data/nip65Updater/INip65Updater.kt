package com.dluvian.nozzle.data.nip65Updater

import com.dluvian.nozzle.data.room.helper.Nip65Relay

interface INip65Updater {
    suspend fun publishAndSaveInDb(nip65Relays: List<Nip65Relay>)
}
