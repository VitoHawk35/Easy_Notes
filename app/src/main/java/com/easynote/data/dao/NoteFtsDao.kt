package com.easynote.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NoteFtsDao {

    @Query("SELECT rowid FROM note_fts WHERE content MATCH :query")
    suspend fun searchNotesByContent(query: String): List<Long>

    @Query("INSERT INTO note_fts(rowid,page_index, content) VALUES(:noteId,:pageIndex, :content)")
    suspend fun insert(noteId: Long, pageIndex: Int, content: String)

    @Query("UPDATE note_fts SET content = :content WHERE rowid = :noteId AND page_index = :pageIndex")
    suspend fun update(noteId: Long, pageIndex: Int, content: String)

    @Query("DELETE FROM note_fts WHERE rowid = :id")
    suspend fun deleteByNoteId(id: Long)

    @Query("DELETE FROM note_fts WHERE rowid = :noteId AND page_index = :pageIndex")
    suspend fun deleteByNoteIdAndPageIndex(noteId: Long, pageIndex: Int)

    @Query("DELETE FROM note_fts WHERE rowid IN (:id)")
    suspend fun deleteByNoteId(id: Set<Long>)
}