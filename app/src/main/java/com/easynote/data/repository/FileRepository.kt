package com.easynote.data.repository

import android.net.Uri

interface FileRepository {
    /**
     * Insert a file path.
     *
     * @param fileName The path of the file to insert.
     */
    suspend fun insertFile(
        noteId: Long,
        pageIndex: Int,
        content: String,
        htmlContent: String,
    )

    /**
     * Save an image to the note's image directory.
     *
     * @param noteId The id of the note.
     * @param imgUri The path of the image to save.
     * @return The new path of the saved image.
     */
    suspend fun saveImage(
        noteId: Long,
        pageIndex: Int,
        imgUri: Uri
    ): String

    /**
     * Delete a note page.
     *
     * @param fileName The path of the file to delete.
     */
    suspend fun deletePage(noteId: Long, pageIndex: Int)

    /**
     * Delete a note files.
     *
     * @param noteId The id of the note to delete files.
     */
    suspend fun deleteFile(noteId: Long)

    suspend fun deleteFile(noteIds: Set<Long>)

    /**
     * update a file path with html content.
     * @param oldName The old path of the file.
     */
    suspend fun updateFile(
        noteId: Long,
        pageIndex: Int,
        content: String,
        htmlContent: String,
    )

    /**
     * Read a file content.
     *
     * @param fileName The path of the file to read.
     * @return The content of the file.
     */
    suspend fun readH5File(noteId: Long, pageIndex: Int): String?


}