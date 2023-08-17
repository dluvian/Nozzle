package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class RelayProvider(
    private val autopilotProvider: IAutopilotProvider,
    private val contactListProvider: IContactListProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao,
) : IRelayProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    // TODO: Determine current pubkey by db table. PubkeyProvider should not be needed
    private var personalPubkey = pubkeyProvider.getPubkey()
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

    override fun getPostRelays(posts: List<PostWithMeta>): List<String> {
        val result = mutableSetOf<String>().apply {
            addAll(getReadRelays())
            addAll(posts.flatMap { it.relays })
            addAll(posts.mapNotNull { it.replyRelayHint })
        }

        return result.toList()
    }

    override suspend fun getAutopilotRelays(): Map<String, Set<String>> {
        val contacts = contactListProvider.listPersonalContactPubkeys().toSet()
        return autopilotProvider.getAutopilotRelays(pubkeys = contacts)
    }

    override suspend fun getReadRelaysOfPubkey(pubkey: String): List<String> {
        return nip65Dao.getReadRelaysOfPubkey(pubkey = pubkey)
    }

    override suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String> {
        return nip65Dao.getWriteRelaysOfPubkey(pubkey = pubkey)
    }

    private fun updateFlow() {
        // TODO: Obsolete this check. See TODO above
        if (personalPubkey != pubkeyProvider.getPubkey()) {
            personalPubkey = pubkeyProvider.getPubkey()
            personalNip65State = nip65Dao.getRelaysOfPubkeyFlow(personalPubkey)
                .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
                .stateIn(
                    scope,
                    SharingStarted.Eagerly,
                    getDefaultRelays().map {
                        Nip65Entity(
                            pubkey = pubkeyProvider.getPubkey(),
                            url = it,
                            isRead = true,
                            isWrite = true,
                            createdAt = 0L
                        )
                    }
                )
        }
    }
}
