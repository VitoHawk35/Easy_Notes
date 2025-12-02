package com.easynote.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easynote.data.annotation.NoteOrderWay
import com.easynote.data.annotation.ORDER_UPDATE_TIME_DESC
import com.easynote.data.entity.NoteEntity
import com.easynote.data.relation.NoteWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteEntityDao {
    /**
     * Insert a note entity into the database.
     */
    @Insert
    suspend fun insert(vararg noteEntity: NoteEntity): LongArray

    /**
     * Insert a single note entity into the database.
     */
    @Insert
    suspend fun insert(noteEntity: NoteEntity): Long

    /**
     * Delete a note entity from the database.
     */
    @Delete
    suspend fun delete(vararg noteEntity: NoteEntity): Int

    /**
     * Delete all note entities from the database.
     */
    @Query("DELETE FROM note")
    suspend fun deleteAll()

    /**
     * Delete a note entity by its ID.
     */
    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM note WHERE id IN (:id)")
    suspend fun deleteById(id: Set<Long>)

    /**
     * Update a note entity in the database.
     */
    @Update
    suspend fun update(vararg noteEntity: NoteEntity)

    /**
     * Update the abstract of a note entity by its ID.
     */
    @Query("UPDATE note SET summary = :summary,update_time = :updateTime WHERE id = :noteId")
    suspend fun updateAbstract(noteId: Long, summary: String, updateTime: Long)


    /**
     * Update the favorite status of a note entity by its ID.
     */
    @Query("UPDATE note SET is_favorite = :isFavor WHERE id = :id")
    suspend fun updateFavor(id: Long, isFavor: Boolean)

    @Query("UPDATE note SET is_favorite = :isFavor WHERE id IN (:id)")
    suspend fun updateFavor(id: Set<Long>, isFavor: Boolean)

    @Query("SELECT * FROM note")
    suspend fun getAll(): List<NoteEntity>

    /**
     * Get all note entities as a live list, ordered by update time descending.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllLive(): LiveData<MutableList<NoteEntity>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT n.*
        FROM  note AS n
        JOIN note_tag_ref AS r ON n.id = r.note_id
        WHERE (:size = 0 OR r.tag_id IN (:tagIds))
        ORDER BY
            CASE WHEN :orderWay = 'UPDATE_TIME_DESC' THEN n.update_time END DESC,
            CASE WHEN :orderWay = 'UPDATE_TIME_ASC' THEN n.update_time END ASC,
            CASE WHEN :orderWay = 'TITLE_ASC' THEN n.title END ASC,
            CASE WHEN :orderWay = 'TITLE_DESC' THEN n.title END DESC
    """
    )
    fun getPagingByTagIds(
        tagIds: Set<Long>? = emptySet(),
        @NoteOrderWay orderWay: String? = ORDER_UPDATE_TIME_DESC,
        size: Int? = tagIds?.size ?: 0
    ): PagingSource<Int, NoteEntity>

    /**
     * Get a note entity by its ID as LiveData.
     */
    @Query("SELECT * FROM note WHERE id = :id")
    fun getLiveById(id: Int): LiveData<NoteEntity>

    /**
     * Get all note entities with their associated tags as LiveData.
     */
    @Transaction
    @Query("SELECT * FROM note")
    fun getAllWithTagsLive(): LiveData<List<NoteWithTags?>>

    /**
     * Get a note entity with its associated tags by note ID as LiveData.
     */
    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId")
    fun getNoteWithTagsLive(noteId: Long): LiveData<NoteWithTags?>

    /**
     * Search note entities by keyword in abstract as Flow.
     *
     */
    @Transaction
    @Query("SELECT * FROM note WHERE (summary LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' )ORDER BY update_time DESC")
    fun searchNotesByAbstractFlow(query: String): PagingSource<Int, NoteWithTags>

    /**
     * Get all note entities with paging support.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllPaging(): PagingSource<Int, NoteEntity>

    /**
     * Get all note entities with their associated tags as a paging source.
     */
    @Transaction
    @Query(
        """
    SELECT DISTINCT n.*
    FROM note AS n
    WHERE
        (:tagSize = 0 OR
            (SELECT COUNT(DISTINCT r.tag_id)
                FROM note_tag_ref AS r
                WHERE r.note_id = n.id AND r.tag_id IN (:tagIds)
            ) = :tagSize
        )
        AND
        (
            :query IS NULL
            OR :query = ''
            OR n.id IN (
                SELECT rowid
                FROM note_content_search
                WHERE content MATCH :query
            )
            OR n.title LIKE '%' || :query || '%'
            OR n.summary LIKE '%' || :query || '%'
        )
        AND
        (:startTime IS NULL OR n.update_time >= :startTime)
        AND
        (:endTime IS NULL OR n.update_time <= :endTime)
    ORDER BY
        CASE WHEN :orderWay = 'UPDATE_TIME_DESC' THEN n.update_time END DESC,
        CASE WHEN :orderWay = 'UPDATE_TIME_ASC' THEN n.update_time END ASC,
        CASE WHEN :orderWay = 'TITLE_ASC' THEN n.title END ASC,
        CASE WHEN :orderWay = 'TITLE_DESC' THEN n.title END DESC
    """
    )
    fun getAllWithTagsPaging(
        tagIds: Set<Long>?,
        query: String?,
        startTime: Long?,
        endTime: Long?,
        @NoteOrderWay orderWay: String? = ORDER_UPDATE_TIME_DESC,
        tagSize: Int? = tagIds?.size ?: 0
    ): PagingSource<Int, NoteWithTags>

    /**
     * Update the update time of a note entity by its ID.
     */
    @Query("UPDATE note SET update_time = :currentTimeMillis WHERE id = :noteId")
    fun updateUpdateTime(noteId: Long, currentTimeMillis: Long)

    @Query(
        """
        SELECT COUNT(DISTINCT n.id)
        FROM note AS n
        JOIN note_tag_ref AS r ON n.id = r.note_id
        WHERE (:size = 0 OR r.tag_id IN (:tagIds))
    """
    )
    suspend fun getCountByTagIds(tagIds: Set<Long>, size: Int? = tagIds.size): Int

    @Transaction
    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun getWithTags(id: Long): NoteWithTags?
}
