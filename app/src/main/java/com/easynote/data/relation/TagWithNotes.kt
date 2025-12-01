package com.easynote.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.easynote.data.entity.NoteEntity
import com.easynote.data.entity.NoteTagCrossRef
import com.easynote.data.entity.TagEntity

data class TagWithNotes(
    @Embedded val tagEntity: TagEntity? = TagEntity(),
    @Relation(
        parentColumn = "id",
        entityColumn = "rowid",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "tag_id",
            entityColumn = "note_id"
        )
    )
    val notes: List<NoteEntity>? = emptyList()
) {
}