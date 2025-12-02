package com.example.mydemo.data.repository

import android.content.Context
import androidx.paging.PagingData
import com.example.mydemo.data.entity.TagEntity
import com.example.mydemo.data.relation.NoteWithTags
import kotlinx.coroutines.flow.Flow

interface Repository {
    /**
     * Create a new note with associated tags.
     *
     * @param noteWithTags The note along with its associated tags.
     * @return The ID of the newly created note.
     */
    suspend fun createNewNote(noteWithTags: NoteWithTags? = null): Long

    /**
     * Create a new tag.
     *
     * @param tagEntity The tag entity to create.
     * @return The ID of the newly created tag.
     */
    suspend fun createNewTag(tagEntity: TagEntity): Long

    /**
     * Delete a note by its entity.
     *
     * @param noteEntity The note entity to delete.
     */
    suspend fun deleteNoteById(noteId: Long)

    /**
     * Delete a specific page from a note.
     *
     * @param noteId The ID of the note.
     * @param pageIndex The index of the page to delete.
     */
    suspend fun deleteNotePage(noteId: Long, pageIndex: Int)

    /**
     * Delete a tag by its ID safely, ensuring no notes are associated with it.
     *
     * @param tagId The ID of the tag to delete.
     */
    suspend fun deleteTagSafelyById(tagId: Int): Boolean

    /**
     * Update the content of a specific page in a note.
     *
     * @param noteId The ID of the note.
     * @param pageIndex The index of the page to update.
     * @param newContent The new content for the specified page.
     */
    suspend fun updateNoteContent(noteId: Long, pageIndex: Int, newContent: String)

    /**
     * Update the tags associated with a specific note.
     *
     * @param noteId The ID of the note.
     * @param tagEntities The list of tag IDs to associate with the note.
     */
    suspend fun updateNoteTags(noteId: Long, tagEntities: List<TagEntity>)


    /**
     * Mark or unmark a note as favourite.
     *
     * @param noteId The ID of the note.
     * @param isFavour True to mark as favourite, false to unmark.
     */
    suspend fun updateNoteFavorite(noteId: Long, isFavour: Boolean)

    /**
     * Get all notes with their associated tags as a paging flow.
     *
     * @return A Flow emitting PagingData of NoteWithTags.
     */
    fun getAllNoteWithTagsPagingFlow(pageSize: Int): Flow<PagingData<NoteWithTags>>

    /**
     * Get the content of a specific page in a note by note ID and page index.
     *
     * @param noteId The ID of the note.
     * @param pageIndex The index of the page.
     * @return The content of the specified page, or null if not found.
     */
    suspend fun getNoteContentByIdAndPageIndex(noteId: Long, pageIndex: Int): String?

    /**
     * Modify the order way of notes.
     *
     * @param way The new order way.
     */
    suspend fun modifyOrderWay(context: Context, way: String)

    /**
     * Get the current order way of notes.
     *
     * @return The current order way.
     */
    suspend fun getOrderWay(context: Context): String

}