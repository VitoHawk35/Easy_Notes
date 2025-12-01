package com.example.mydemo.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.mydemo.data.entity.NoteEntity
import com.example.mydemo.data.entity.NoteTagCrossRef
import com.example.mydemo.data.entity.TagEntity

data class TagWithNotes(
    @Embedded val tagEntity: TagEntity? = TagEntity(),
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "tag_id",
            entityColumn = "note_id"
        )
    )
    val notes: List<NoteEntity>? = emptyList()
) {
}