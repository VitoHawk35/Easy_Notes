package com.easynote.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.easynote.data.entity.NoteEntity
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
     * Update the content of a note.
     *
     * @param noteId
     * @param newContent
     */
    suspend fun updateNoteContent(noteId: Long, newContent: String)

    /**
     * Update the favor status of a note by its ID.
     *
     * @param id
     * @param isFavor
     */
    suspend fun updateNoteFavor(id: Int, isFavor: Boolean)

    /**
     * get all notes as a list.
     * @return List<NoteEntity>
     */
    suspend fun getAllNotes(): List<NoteEntity>

    /**
     * Get a note by its ID.
     * @return NoteEntity
     */
    suspend fun getNoteById(id: Int): NoteEntity

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
    fun getAllNoteWithTagsPagingFlow(pageSize: Int): Flow<PagingData<NoteWithTags>>

}
