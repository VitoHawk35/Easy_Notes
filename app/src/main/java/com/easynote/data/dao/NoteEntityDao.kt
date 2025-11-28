package com.easynote.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easynote.data.entity.NoteEntity
import com.easynote.data.relation.NoteWithTags

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

    /**
     * Update a note entity in the database.
     */
    @Update
    suspend fun update(vararg noteEntity: NoteEntity)

    /**
     * Update the content of a note entity by its ID.
     */
    @Query("UPDATE note SET content = :newContent, update_time = CURRENT_TIMESTAMP WHERE id = :noteId")
    suspend fun updateNoteContent(noteId: Long, newContent: String)

    /**
     * Update the favorite status of a note entity by its ID.
     */
    @Query("UPDATE note SET is_favorite = :isFavor WHERE id = :id")
    suspend fun updateFavor(id: Int, isFavor: Boolean)

    @Query("SELECT * FROM note")
    suspend fun getAll(): List<NoteEntity>

    /**
     * Get all note entities as a live list, ordered by update time descending.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllLive(): LiveData<MutableList<NoteEntity>>

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
     * Search note entities by keyword in content as LiveData.
     */
    @Query(
        "SELECT * FROM note " +
                "WHERE (note.content LIKE '%' || :keyword || '%' OR note.title LIKE '%' || :keyword || '%')" +
                " ORDER BY note.update_time DESC"
    )
    fun searchNotesLive(keyword: String): LiveData<List<NoteEntity>>

    /**
     * Get all note entities with paging support.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllPaging(): PagingSource<Int, NoteEntity>

    /**
     * Get all note entities with their associated tags as a paging source.
     */
    @Transaction
    @Query("SELECT * FROM note")
    fun getAllWithTagsPaging(): PagingSource<Int, NoteWithTags>
}
