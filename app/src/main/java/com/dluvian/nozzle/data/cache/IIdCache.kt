package com.dluvian.nozzle.data.cache

import com.dluvian.nozzle.model.NoteId

interface IIdCache {
    fun addPostIds(ids: Collection<String>): Boolean
    fun containsPostId(id: String): Boolean
    fun getPostIds(): Set<String>
    fun clearPostIds()
    fun removePostId(noteId: NoteId)

    fun addPubkeys(pubkeys: Collection<String>): Boolean
    fun getPubkeys(): Set<String>
    fun clearPubkeys()

    fun addNip65Authors(pubkeys: Collection<String>): Boolean
    fun getNip65Authors(): Set<String>
    fun clearNip65Authors()

    fun addContactListAuthors(pubkeys: Collection<String>): Boolean
    fun getContactListAuthors(): Set<String>
    fun clearContactListAuthors()
}
