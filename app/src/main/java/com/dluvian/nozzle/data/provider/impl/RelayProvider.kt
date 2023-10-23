package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
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
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val nip65Dao: Nip65Dao,
) : IRelayProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    // TODO: Determine current pubkey by db table. PubkeyProvider should not be needed
    private var personalPubkey = pubkeyProvider.getActivePubkey()
    private var personalNip65State = nip65Dao.getRelaysOfPubkeyFlow(personalPubkey)
        .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        .stateIn(
            scope, SharingStarted.Eagerly, emptyList()
        )

    override fun getReadRelays(): List<String> {
        updateFlow()
        return personalNip65State.value
            .filter { it.isRead }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
    }

    override fun getWriteRelays(): List<String> {
        updateFlow()
        return personalNip65State.value
            .filter { it.isWrite }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
    }

    override fun getNip65Relays(): List<Nip65Relay> {
        updateFlow()
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

    private fun updateFlow() {
        // TODO: Obsolete this check. See TODO above
        if (personalPubkey == pubkeyProvider.getActivePubkey()) return
        personalPubkey = pubkeyProvider.getActivePubkey()
        personalNip65State = nip65Dao.getRelaysOfPubkeyFlow(personalPubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                getDefaultNip65s()
            )
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
