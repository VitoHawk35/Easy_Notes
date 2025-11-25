package com.example.mydemo.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mydemo.data.entity.NoteEntity
import com.example.mydemo.data.entity.TagEntity
import com.example.mydemo.data.model.NoteModel

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
     * Delete all note entities from the database.
     */
    @Query("DELETE FROM note")
    fun deleteAll()

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
     * Get all note entities with their associated tag information as LiveData.
     */
    @Query(
        "SELECT note.id,note.title,note.content," +
                "note.create_time,note.update_time,note.is_favorite,tag.name,tag.color " +
                "FROM note " +
                "INNER JOIN tag ON note.tag_id = tag.id;"
    )
    fun getAllWithTagLive(): LiveData<List<NoteModel>>

    /**
     * Get all note entities mapped to their tag entities as LiveData.
     */
    @Query("SELECT * FROM note JOIN tag ON note.tag_id = tag.id WHERE tag.id = :id")
    fun getTagMapNotes(id: Int): LiveData<Map<TagEntity, NoteEntity>>

    /**
     * Search note entities by keyword in content as LiveData.
     */
    @Query("SELECT * FROM note " +
            "WHERE (note.content LIKE '%' || :keyword || '%' OR note.title LIKE '%' || :keyword || '%')" +
            " ORDER BY note.update_time DESC")
    fun searchNotesLive(keyword: String): LiveData<List<NoteEntity>>

    /**
     * Get all note entities with paging support.
     */
    @Query("SELECT * FROM note ORDER BY update_time DESC")
    fun getAllPaging(): PagingSource<Int, NoteEntity>


}
