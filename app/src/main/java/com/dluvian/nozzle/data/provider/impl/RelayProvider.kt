package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDebounce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Collections


class RelayProvider(
    private val autopilotProvider: IAutopilotProvider,
    private val contactListProvider: IContactListProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao,
) : IRelayProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)
    private val cache: MutableList<String> = Collections.synchronizedList(mutableListOf())

    // TODO: Determine current pubkey by db table. PubkeyProvider should not be needed
    private var personalPubkey = pubkeyProvider.getPubkey()
    private var personalNip65State = nip65Dao.getRelaysOfPubkeyFlow(personalPubkey)
        .stateIn(
            scope, SharingStarted.Eagerly, emptyList()
        )

    init {
        cache.addAll(getDefaultRelays())
    }

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

    override suspend fun getAutopilotRelays(): Map<String, Set<String>> {
        val contacts = contactListProvider.listPersonalContactPubkeys().toSet()
        val result = autopilotProvider.getAutopilotRelays(pubkeys = contacts)

        val newRelays = result.keys.minus(cache.toSet())
        if (newRelays.isNotEmpty()) cache.addAll(0, newRelays)

        return result
    }

    private fun updateFlow() {
        // TODO: Obsolete this check. See TODO above
        if (personalPubkey != pubkeyProvider.getPubkey()) {
            personalPubkey = pubkeyProvider.getPubkey()
            personalNip65State = nip65Dao.getRelaysOfPubkeyFlow(personalPubkey)
                .firstThenDebounce(NORMAL_DEBOUNCE)
                .stateIn(
                    scope, SharingStarted.Eagerly, emptyList()
                )
        }
    }
}
