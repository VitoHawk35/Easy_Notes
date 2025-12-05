package com.easynote.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NoteFtsDao {

    @Query("SELECT id FROM note_fts WHERE note_fts MATCH :query")
    suspend fun searchNotesByContent(query: String): List<Long>

    @Query("INSERT INTO note_fts(id,page_index,title,summary,content) VALUES(:noteId,:pageIndex,:title,:summary,:content)")
    suspend fun insert(
        noteId: Long,
        pageIndex: Int? = null,
        title: String? = null,
        summary: String? = null,
        content: String? = null
    )

    @Query("SELECT id FROM note_fts WHERE id = :noteId AND (page_index IS NULL OR page_index = :pageIndex)")
    suspend fun getNoteFtsId(noteId: Long, pageIndex: Int?): Long?

    @Query(
        """
        UPDATE note_fts 
        SET 
            title = CASE WHEN :title IS NOT NULL THEN :title ELSE title END,
            summary = CASE WHEN :summary IS NOT NULL THEN :summary ELSE summary END,
            content = CASE WHEN :content IS NOT NULL THEN :content ELSE content END
        WHERE id = :noteId AND (page_index IS NULL OR page_index = :pageIndex)
    """
    )
    suspend fun update(
        noteId: Long,
        pageIndex: Int? =null,
        title: String? = null,
        summary: String? = null,
        content: String? = null
    )

    @Query("DELETE FROM note_fts WHERE id = :id")
    suspend fun deleteByNoteId(id: Long)

    @Query("DELETE FROM note_fts WHERE id = :noteId AND page_index = :pageIndex")
    suspend fun deleteByNoteIdAndPageIndex(noteId: Long, pageIndex: Int)

    @Query("DELETE FROM note_fts WHERE id IN (:id)")
    suspend fun deleteByNoteId(id: Set<Long>)
}