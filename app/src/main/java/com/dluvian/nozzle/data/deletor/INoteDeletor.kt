package com.dluvian.nozzle.data.deletor

import com.dluvian.nozzle.model.NoteId

interface INoteDeletor {
    fun deleteNote(noteId: NoteId)
}
