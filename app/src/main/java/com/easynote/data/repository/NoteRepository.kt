package com.easynote.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.easynote.data.annotation.NoteOrderWay
import com.easynote.data.annotation.UPDATE_TIME_DESC
import com.easynote.data.entity.NoteEntity
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    /**
     * Insert a new note.
     *
     * @param noteEntity
     */
    suspend fun insertNote(noteEntity: NoteEntity): Long

    /**
     * Insert a new note with associated tags.
     *
     * @param noteWithTags
     */
    suspend fun insertNoteWithTags(noteWithTags: NoteWithTags): Long

    /**
     * Delete a note.
     *
     * @param noteEntity
     */
    suspend fun deleteNote(vararg noteEntity: NoteEntity): Int

    /**
     * Delete a note by its ID.
     *
     * @param id
     */
    suspend fun deleteNoteById(id: Long)

    /**
     * Delete multiple notes by their IDs.
     *
     * @param id
     */
    suspend fun deleteNoteById(id: Set<Long>)

    /**
     * Delete a specific page from a note.
     *
     * @param noteId
     * @param pageIndex
     */
    suspend fun deleteNotePage(noteId: Long, pageIndex: Int)

    /**
     * Update a note.
     *
     * @param noteEntity
     */
    suspend fun updateNote(vararg noteEntity: NoteEntity)

    /**
     * Update the favor status of a note by its ID.
     *
     * @param id
     * @param isFavor
     */
    suspend fun updateNoteFavor(id: Long, isFavor: Boolean)

    suspend fun updateNoteFavor(id: Set<Long>, isFavor: Boolean)

    /**
     * Update the tags associated with a note.
     *
     * @param id
     * @param tagEntity
     */
    suspend fun updateNoteTags(id: Long, vararg tagEntity: TagEntity)

    /**
     * Update the abstract of a note.
     *
     * @param noteId
     * @param abstract
     */
    suspend fun updateTitleOrSummary(noteId: Long, title: String? = null, summary: String? = null)

    /**
     * get all notes as a list.
     * @return List<NoteEntity>
     */
    suspend fun getAllNotes(): List<NoteEntity>

    /**
     * Get a note by its ID.
     * @return NoteEntity
     */
    suspend fun getNoteById(id: Long): NoteWithTags?

    /**
     * Get notes by tag IDs with pagination support as a flow.
     * @return Flow<PagingData<NoteEntity>>
     */
    fun getNoteByTagIdPagingFlow(
        TagIds: Set<Long>? = emptySet(),
        pageSize: Int,
        @NoteOrderWay orderWay: String? = UPDATE_TIME_DESC
    ): Flow<PagingData<NoteEntity>>

    /**
     * Get all notes as LiveData.
     * @return LiveData<List<NoteEntity>>
     */
    fun getNoteByIdLive(id: Int): LiveData<NoteEntity>

    /**
     * Get all notes as LiveData.
     * @return LiveData<MutableList<NoteEntity>>
     */
    fun getAllNotesLive(): LiveData<MutableList<NoteEntity>>

    /**
     * Get all notes with pagination support.
     * @return LiveData<PagingData<NoteEntity>>
     */
    fun getAllNotesPaging(pageSize: Int): LiveData<PagingData<NoteEntity>>

    /**
     * Get all notes with their associated tags as a paging flow.
     * @return Flow<PagingData<NoteWithTags>>
     */
    fun getAllNotePagingFlow(
        pageSize: Int,
        tagIds: Set<Long>?,
        query: String?,
        startTime: Long?,
        endTime: Long?,
        @NoteOrderWay orderWay: String?
    ): Flow<PagingData<NoteWithTags>>

    /**
     * Search notes by a query string with pagination support as a flow.
     * @return Flow<PagingData<NoteWithTags>>
     */
    fun searchNotesByQueryFlow(query: String, pageSize: Int): Flow<PagingData<NoteWithTags>>

    /**
     * Update the update time of a note.
     *
     * @param noteId
     */
    suspend fun updateNoteUpdateTime(noteId: Long)

    /**
     * Get the count of notes associated with specific tag IDs.
     *
     * @param tagIds The set of tag IDs to filter notes.
     * @return The count of notes that match the tag IDs.
     */
    suspend fun getNoteCountByTags(tagIds: Set<Long>): Int

    /**
     * Update the search table for a note.
     *
     * @param noteId
     * @param content
     */
    suspend fun updateSearchTable(
        noteId: Long,
        pageIndex: Int? = null,
        title: String? = null,
        summary: String? = null,
        content: String? = null
    )

    /**
     * Get all notes with their associated tags as a flow.
     *
     * @return A Flow emitting a list of NoteWithTags.
     */
    fun getAllNoteFlow(
        query: String?,
        tagIds: Set<Long>?,
        startTime: Long?,
        endTime: Long?,
        orderWay: String?
    ): Flow<List<NoteWithTags>>


}
