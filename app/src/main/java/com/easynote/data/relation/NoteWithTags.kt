package com.easynote.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.easynote.data.entity.NoteEntity
import com.easynote.data.entity.NoteTagCrossRef
import com.easynote.data.entity.TagEntity

data class NoteWithTags(
    @Embedded val noteEntity: NoteEntity? = NoteEntity(),
    @Relation(
        parentColumn = "rowid",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "note_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>? = emptyList()
) {
}