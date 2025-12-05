package com.easynote.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NoteContentSearchDao {

    @Query("SELECT rowid FROM note_content_search WHERE content MATCH :query")
    suspend fun searchNotesByContent(query: String): List<Long>

    @Query("INSERT INTO note_content_search(rowid,page_index, content) VALUES(:noteId,:pageIndex, :content)")
    suspend fun insert(noteId: Long, pageIndex: Int, content: String)

    @Query("UPDATE note_content_search SET content = :content WHERE rowid = :noteId AND page_index = :pageIndex")
    suspend fun update(noteId: Long, pageIndex: Int, content: String)

    @Query("DELETE FROM note_content_search WHERE rowid = :id")
    suspend fun deleteByNoteId(id: Long)

    @Query("DELETE FROM note_content_search WHERE rowid = :noteId AND page_index = :pageIndex")
    suspend fun deleteByNoteIdAndPageIndex(noteId: Long, pageIndex: Int)

    @Query("DELETE FROM note_content_search WHERE rowid IN (:id)")
    suspend fun deleteByNoteId(id: Set<Long>)
}