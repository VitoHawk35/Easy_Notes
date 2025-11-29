package com.easynote.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.TagWithNotes
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    fun getPagingTagsFlow(pageSize: Int): Flow<PagingData<TagEntity>>

    fun getAllTagsFlow(): Flow<List<TagEntity>>

    /**
     * Insert a new tag.
     *
     * @param tagEntity
     */
    suspend fun insertTag(tagEntity: TagEntity): Long

    /**
     * Delete a tag.
     *
     * @param tagEntity
     */
    suspend fun deleteTag(vararg tagEntity: TagEntity)

    /**
     * Delete a tag by its ID.
     *
     * @param id
     */
    suspend fun deleteTagById(id: Long)

    /**
     * Update tag.
     *
     * @param tagEntity
     */
    suspend fun updateTag(vararg tagEntity: TagEntity)

    /**
     * Get a tag by its ID as LiveData.
     * @param id
     * @return LiveData<TagEntity>
     */
    fun getTagByIdLive(id: Int): LiveData<TagEntity>

    /**
     * Get tag with associated notes by tag ID.
     *
     * @param id
     * @return List<TagWithNotes>
     */
    suspend fun getTagWithNotesById(id: Long): TagWithNotes


}