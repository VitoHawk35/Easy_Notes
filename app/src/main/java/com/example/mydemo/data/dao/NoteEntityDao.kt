package com.example.mydemo.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mydemo.data.entity.NoteEntity

@Dao
interface NoteEntityDao {

    /**
     * Insert a note entity into the database.
     */
    @Insert
    fun insert(vararg noteEntity: NoteEntity)

    /**
     * Delete a note entity from the database.
     */
    @Delete
    fun delete(vararg noteEntity: NoteEntity)

    /**
     * Delete a note entity by its ID.
     */
    @Query("DELETE FROM note WHERE id = :id")
    fun deleteById(id: Int)

    /**
     * Update a note entity in the database.
     */
    @Update
    fun update(vararg noteEntity: NoteEntity)

    /**
     * Get all note entities as a live list, ordered by update time descending.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllLive(): LiveData<List<NoteEntity>>

    /**
     * Get a note entity by its ID as LiveData.
     */
    @Query("SELECT * FROM note WHERE id = :id")
    fun getLiveById(id: Int): LiveData<NoteEntity>

    /**
     * Get all note entities with paging support.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllPaging(): PagingSource<Int, NoteEntity>
}
