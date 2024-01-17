package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.SHORT_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.data.utils.getMaxRelays
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
        .firstThenDistinctDebounce(SHORT_DEBOUNCE)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun getReadRelays(limit: Boolean): List<Relay> {
        val relays = personalNip65State.value
            .filter { it.isRead }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
        return if (limit) getMaxRelays(from = relays) else relays
    }

    override fun getWriteRelays(): List<Relay> {
        return personalNip65State.value
            .filter { it.isWrite }
            .map { it.url }
            .ifEmpty { getDefaultRelays() }
    }

    override fun getNip65Relays(): List<Nip65Relay> {
        return personalNip65State.value.ifEmpty { getDefaultNip65s() }
    }

    override suspend fun getReadRelaysOfPubkey(pubkey: Pubkey): List<Relay> {
        return nip65Dao.getReadRelaysOfPubkey(pubkey = pubkey)
    }

    override suspend fun getReadRelaysOfPubkeys(pubkeys: Collection<Pubkey>): Map<Pubkey, List<Relay>> {
        val dbResult = nip65Dao.getReadRelaysByPubkeys(pubkeys = pubkeys.distinct()).toMutableMap()
        pubkeys.forEach { pubkey -> dbResult.putIfAbsent(pubkey, emptyList()) }

        return dbResult
    }

    override suspend fun getWriteRelaysOfPubkey(pubkey: Pubkey): List<Relay> {
        return nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
    }

    override suspend fun getWriteRelaysByPubkeys(pubkeys: Collection<Pubkey>): Map<Pubkey, List<Relay>> {
        val dbResult = nip65Dao.getWriteRelaysByPubkeys(pubkeys = pubkeys.distinct()).toMutableMap()
        pubkeys.forEach { pubkey -> dbResult.putIfAbsent(pubkey, emptyList()) }

        return dbResult
    }

    override suspend fun getRelaysOfContacts(): List<Relay> {
        // TODO: Remove contactListProvider. Use account query
        val pubkeys = contactListProvider.listPersonalContactPubkeysOrDefault()
        return nip65Dao.getRelaysOfPubkeys(pubkeys = pubkeys)
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
