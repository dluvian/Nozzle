package com.dluvian.nozzle.data.postCardInteractor

import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey

interface IPostCardInteractor {
    fun like(noteId: NoteId, postPubkey: Pubkey)
    fun deleteLike(noteId: NoteId)
}
