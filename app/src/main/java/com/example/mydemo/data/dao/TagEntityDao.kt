package com.example.mydemo.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mydemo.data.entity.TagEntity

@Dao
interface TagEntityDao {
    /**
     * Insert a tag entity into the database.
     * @param tagEntities
     */
    @Insert
    fun insert(vararg tagEntities: TagEntity)

    /**
     * Delete a tag entity from the database.
     * @param tagEntities
     */
    @Delete
    fun delete(vararg tagEntities: TagEntity)

    /**
     * Delete a tag entity by its ID.
     * @param id
     */
    @Query("DELETE FROM tag WHERE id = :id")
    fun deleteById(id: Int)

    /**
     * Update a tag entity in the database.
     * @param tagEntities
     */
    @Update
    fun update(vararg tagEntities: TagEntity)

    /**
     * Get tag entity as LiveData.
     * @return LiveData<TagEntity>
     */
    @Query("SELECT * FROM tag WHERE id = :id")
    fun getByIdLive(id: Int): LiveData<TagEntity>

    /**
     * Get all tag entities as a live list, ordered by id descending.
     * @return LiveData<MutableList<TagEntity>>
     */
    @Query("SELECT * FROM tag ORDER BY id DESC")
    fun getAllLive(): LiveData<List<TagEntity>>

    /**
     * Get all tag entities with paging support.
     * @return PagingSource<Int, TagEntity>
     */
    @Query("SELECT * FROM tag ORDER BY id DESC")
    fun getAllPaging(): PagingSource<Int, TagEntity>
}
