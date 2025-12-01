package com.easynote.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.easynote.data.entity.NoteTagCrossRef

@Dao
interface NoteTagCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRefs: List<NoteTagCrossRef>)

    @Query("DELETE FROM note_tag_ref WHERE note_id = :noteId")
    suspend fun deleteCrossRefsByNoteId(noteId: Long)

    @Transaction
    suspend fun insertNoteWithTags(
        noteId: Long,
        tagsIds: List<Long>
    ) {
        val crossRefs = tagsIds.map { tagId ->
            NoteTagCrossRef(
                noteId = noteId,
                tagId = tagId
            )
        }
        insertCrossRef(crossRefs)
    }

    @Transaction
    suspend fun updateNoteTags(
        noteId: Long,
        newTagIds: List<Long>
    ) {
        deleteCrossRefsByNoteId(noteId)
        val crossRefs = newTagIds.map { tagId ->
            NoteTagCrossRef(
                noteId = noteId,
                tagId = tagId
            )
        }
        insertCrossRef(crossRefs)
    }

}