package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class RelayProvider(
    private val contactListProvider: IContactListProvider,
    private val nip65Dao: Nip65Dao,
) : IRelayProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    private val personalNip65State = nip65Dao.getPersonalRelaysFlow()
        .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun getReadRelays(): List<String> {
        return personalNip65State.value
            .filter { it.isRead }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
    }

    override fun getWriteRelays(): List<String> {
        return personalNip65State.value
            .filter { it.isWrite }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
    }

    override fun getNip65Relays(): List<Nip65Relay> {
        return personalNip65State.value.ifEmpty { getDefaultNip65s() }
    }

    override suspend fun getReadRelaysOfPubkey(pubkey: String): List<String> {
        return nip65Dao.getReadRelaysOfPubkey(pubkey = pubkey)
    }

    override suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String> {
        return nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
    }

    override suspend fun getWriteRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>> {
        return nip65Dao.getWriteRelaysOfPubkeys(pubkeys = pubkeys)
    }

    override suspend fun getRelaysOfContacts(): List<Relay> {
        return nip65Dao.getRelaysOfPubkeys(pubkeys = contactListProvider.listPersonalContactPubkeys())
    }

    private fun getDefaultNip65s() = getDefaultRelays()
        .map {
            Nip65Relay(
                url = it,
                isRead = true,
                isWrite = true,
            )
        }
}
