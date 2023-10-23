package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

private const val TAG = "ContactListProvider"

class ContactListProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val contactDao: ContactDao
) : IContactListProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    // TODO: Don't track it. Use extra DB table for current user
    private var personalPubkey = pubkeyProvider.getActivePubkey()
    private var personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun listPersonalContactPubkeys(): List<String> {
        return if (personalPubkey != pubkeyProvider.getActivePubkey()) {
            Log.i(TAG, "Pubkey changed. Update contact list flow")
            updatePubkeyAndContactListFlow(newPubkey = pubkeyProvider.getActivePubkey())
            contactDao.listContactPubkeys(pubkey = pubkeyProvider.getActivePubkey())
        } else {
            personalContactListState.value
        }
    }

    override fun getPersonalContactPubkeysFlow(): Flow<List<String>> {
        return personalContactListState
    }

    private fun updatePubkeyAndContactListFlow(newPubkey: String) {
        personalPubkey = newPubkey
        personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }
}
