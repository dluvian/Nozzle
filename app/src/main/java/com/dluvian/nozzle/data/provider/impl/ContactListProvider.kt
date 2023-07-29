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

    // TODO: Determine current pubkey by db table. PubkeyProvider should not be needed
    private var personalPubkey = pubkeyProvider.getPubkey()
    private var personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
        .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        .stateIn(
            scope, SharingStarted.Eagerly, emptyList()
        )

    override fun listPersonalContactPubkeys(): List<String> {
        // TODO: Obsolete this check. See TODO above
        if (personalPubkey != pubkeyProvider.getPubkey()) {
            personalPubkey = pubkeyProvider.getPubkey()
            personalContactListState = contactDao.listContactPubkeysFlow(personalPubkey)
                .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
                .stateIn(
                    scope, SharingStarted.Eagerly, emptyList()
                )
        }
        Log.i(TAG, "Return ${personalContactListState.value.size} pubkeys")
        return personalContactListState.value
    }
}
