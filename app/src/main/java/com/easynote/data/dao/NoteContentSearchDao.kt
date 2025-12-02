package com.easynote.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NoteContentSearchDao {

    @Query("SELECT rowid FROM note_content_search WHERE content MATCH :query")
    suspend fun searchNotesByContent(query: String): List<Long>

    @Query("INSERT INTO note_content_search(rowid, content) VALUES(:noteId, :content)")
    suspend fun insert(noteId:Long,content: String)
}