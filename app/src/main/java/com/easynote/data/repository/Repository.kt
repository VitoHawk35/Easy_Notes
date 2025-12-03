package com.easynote.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.PagingData
import com.easynote.data.annotation.NoteOrderWay
import com.easynote.data.annotation.UPDATE_TIME_DESC
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
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
     * Delete multiple notes by their IDs.
     *
     * @param noteId The set of note IDs to delete.
     */
    suspend fun deleteNoteById(noteId: Set<Long>)

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
    suspend fun deleteTagSafelyById(tagId: Long): Boolean


    /**
     * Update the content of a note.
     *
     * @param noteId The ID of the note.
     * @param newContent The new content for the note.
     * @param newHTMLContent The new HTML content for the note.
     * @param imgPath Optional image paths associated with the note.
     */
    suspend fun updateNoteContent(
        noteId: Long,
        pageIndex: Int,
        newContent: String,
        newHTMLContent: String = "",
    )

    /**
     * Update the abstract of a note.
     *
     * @param noteId The ID of the note.
     * @param summary The new abstract for the note.
     */
    suspend fun updateTitleOrTitle(noteId: Long, title: String, summary: String)

    /**
     * Save an image associated with a note.
     *
     * @param noteId The ID of the note.
     * @param pageIndex The index of the page.
     * @param imgUri The path of the image to save.
     * @return The path where the image is saved.
     */
    suspend fun saveImage(
        noteId: Long,
        pageIndex: Int,
        imgUri: Uri
    ): String

    /**
     * Update the tags associated with a specific note.
     *
     * @param noteId The ID of the note.
     * @param tagEntities The list of tag IDs to associate with the note.
     */
    suspend fun updateNoteTags(noteId: Long, vararg tagEntities: TagEntity)

    /**
     * Mark or unmark a note as favourite.
     *
     * @param noteId The ID of the note.
     * @param isFavour True to mark as favourite, false to unmark.
     */
    suspend fun updateNoteFavorite(noteId: Long, isFavour: Boolean)

    suspend fun updateNoteFavorite(noteIds: Set<Long>, isFavour: Boolean)

    /**
     * Get all notes with their associated tags as a paging flow.
     *
     * @return A Flow emitting PagingData of NoteWithTags.
     */
    fun getAllNoteWithTagsPagingFlow(
        pageSize: Int,
        query: String? = null,
        tagIds: Set<Long>? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        @NoteOrderWay orderWay: String? = UPDATE_TIME_DESC
    ): Flow<PagingData<NoteWithTags>>

    /**
     * Get the count of notes associated with specific tag IDs.
     *
     * @param tagIds The set of tag IDs to filter notes.
     * @return The count of notes that match the tag IDs.
     */
    fun getAllNoteWithTagsFlow(
        query: String? = null,
        tagIds: Set<Long>? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        @NoteOrderWay orderWay: String? = UPDATE_TIME_DESC
    ): Flow<List<NoteWithTags>>

    /**
     * Get all tags as a paging flow.
     *
     * @return A Flow emitting PagingData of TagEntity.
     */
    fun getAllTagsFlow(pageSize: Int): Flow<PagingData<TagEntity>>

    /**
     * Get the content of a specific page in a note by note ID and page index.
     *
     * @param noteId The ID of the note.
     * @param pageIndex The index of the page.
     * @return The content of the specified page, or null if not found.
     */
    suspend fun getNoteContentByIdAndPageIndex(noteId: Long, pageIndex: Int): String?

    /**
     * Get a note along with its associated tags by note ID.
     *
     * @param noteId The ID of the note.
     * @return The NoteWithTags object, or null if not found.
     */
    suspend fun getNoteWithTagsById(noteId: Long): NoteWithTags?


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