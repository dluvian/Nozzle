package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.FRIEND_CIRCLE_LIMIT
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ContactListProvider(
    contactDao: ContactDao
) : IContactListProvider {
    private val scope = CoroutineScope(context = Dispatchers.Default)

    private val personalContactListState = contactDao.listPersonalContactPubkeysFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val friendsOfFriends = contactDao.listFriendsOfFriendsFlow(limit = FRIEND_CIRCLE_LIMIT)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())


    override fun listPersonalContactPubkeys(): List<String> {
        return personalContactListState.value
    }

    override fun getPersonalContactPubkeysFlow(): Flow<List<String>> {
        return personalContactListState
    }

    override fun listFriendCirclePubkeys(): Set<Pubkey> {
        return (listPersonalContactPubkeys().toSet() + friendsOfFriends.value)
    }
}
