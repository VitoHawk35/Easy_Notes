package com.easynote.data.repository

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
        vararg imgPaths: String
    )

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

    /**
     * update a file path.
     * @param oldName The old path of the file.
     */
    suspend fun updateFile(noteId: Long, pageIndex: Int, newContent: String)

    /**
     * update a file path with html content.
     * @param oldName The old path of the file.
     */
    suspend fun updateFile(
        noteId: Long,
        pageIndex: Int,
        content: String,
        htmlContent: String,
        vararg imgPaths: String
    )

    /**
     * Read a file content.
     *
     * @param fileName The path of the file to read.
     * @return The content of the file.
     */
    suspend fun readH5File(noteId: Long, pageIndex: Int): String?

    /**
     * Read a txt file content.
     *
     * @param fileName The path of the file to read.
     * @return The content of the file.
     */
    suspend fun readTxtFile(noteId: Long, pageIndex: Int): String?


}