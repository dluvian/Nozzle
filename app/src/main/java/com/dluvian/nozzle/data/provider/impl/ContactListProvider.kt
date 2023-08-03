package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

private const val TAG = "ContactListProvider"

class ContactListProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val contactDao: ContactDao
) : IContactListProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    private var personalPubkey = pubkeyProvider.getPubkey()
    private var personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
        .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun listPersonalContactPubkeys(): List<String> {
        return if (personalPubkey != pubkeyProvider.getPubkey()) {
            Log.i(TAG, "Pubkey changed. Update contact list flow")
            updatePubkeyAndContactListFlow(newPubkey = pubkeyProvider.getPubkey())
            contactDao.listContactPubkeys(pubkey = pubkeyProvider.getPubkey())
        } else {
            personalContactListState.value
        }
    }

    private fun updatePubkeyAndContactListFlow(newPubkey: String) {
        personalPubkey = newPubkey
        personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }
}
