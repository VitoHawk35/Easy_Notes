package com.example.mydemo.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.mydemo.data.entity.NoteEntity
import com.example.mydemo.data.entity.NoteTagCrossRef
import com.example.mydemo.data.entity.TagEntity

data class NoteWithTags(
    @Embedded val noteEntity: NoteEntity? = NoteEntity(),
    @Relation(
        parentColumn = "id",
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