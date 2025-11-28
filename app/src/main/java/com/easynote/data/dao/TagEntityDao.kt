package com.easynote.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easynote.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagEntityDao {
    /**
     * Insert a tag entity into the database.
     * @param tagEntities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tagEntity: TagEntity): Long

    /**
     * Delete a tag entity from the database.
     * @param tagEntities
     */
    @Delete
    suspend fun delete(vararg tagEntities: TagEntity)

    /**
     * Delete all tag entities from the database.
     */
    @Query("DELETE FROM tag")
    suspend fun deleteAll()

    /**
     * Delete a tag entity by its ID.
     * @param id
     */
    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Update a tag entity in the database.
     * @param tagEntities
     */
    @Update
    suspend fun update(vararg tagEntities: TagEntity)

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
    fun getAllLive(): LiveData<MutableList<TagEntity>>

    /**
     * Get all tag entities with paging support.
     * @return PagingSource<Int, TagEntity>
     */
    @Query("SELECT * FROM tag ORDER BY id DESC")
    fun getAllPaging(): PagingSource<Int, TagEntity>

    /**
     * Get paging source for tags.
     * @return PagingSource<Int, TagEntity>
     */
    @Query("SELECT * FROM tag ORDER BY id DESC")
    fun getPagingTags(): PagingSource<Int, TagEntity>

    /**
     * Get all tag entities as a flow list, ordered by id descending.
     * @return Flow<List<TagEntity>>
     */
    @Query("SELECT * FROM tag ORDER BY id DESC")
    fun getAllFlow(): Flow<List<TagEntity>>
}
