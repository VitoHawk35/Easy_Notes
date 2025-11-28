package com.easynote.data.repository.impl

import android.app.Application
import android.content.Context
import com.easynote.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepositoryImpl(application: Application) : FileRepository {
    private val context: Context = application.applicationContext

    private fun getFileName(noteId: Long, pageIndex: Int): String {
        return "${noteId}/${pageIndex}/"
    }

    override suspend fun insertFile(noteId: Long, pageIndex: Int, content: String) = withContext(
        Dispatchers.IO
    ) {
        File(context.filesDir, getFileName(noteId, pageIndex)).apply {
            parentFile?.mkdirs()
            writeText(content)
        }
        return@withContext
    }

    override suspend fun deleteFile(noteId: Long, pageIndex: Int) =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, noteId.toString())
            if (!dir.exists() || !dir.isDirectory) {
                return@withContext
            }
            val file = File(dir, "${pageIndex}.txt")
            if (file.exists()) {
                file.delete()
            }

            val filesToShift = dir.listFiles { file ->
                file.isFile && file.name.endsWith(".txt")
            }?.mapNotNull { file ->
                val nameWithoutExt = file.name.substringBefore(".txt")
                nameWithoutExt.toIntOrNull()?.let { index ->
                    index to file
                }
            }?.filter { (index, _) ->
                index > pageIndex
            }?.sortedBy { (index, _) ->
                index
            } ?: emptyList()

            for ((index, file) in filesToShift) {
                val newFile = File(dir, "{$index - 1}.txt")
                file.renameTo(newFile)
            }
        }

    override suspend fun deleteFile(noteId: Long) =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, noteId.toString())
            if (dir.exists() && dir.isDirectory) {
                dir.deleteRecursively()
            }
        }

    override suspend fun updateFile(
        noteId: Long,
        pageIndex: Int,
        newContent: String
    ) = withContext(Dispatchers.IO) {
        val fileName = getFileName(noteId = noteId, pageIndex)
        val f = File(context.filesDir, fileName)
        if (f.exists()) {
            f.delete()
        }
        f.writeText(newContent)
    }

    override suspend fun readFile(noteId: Long, pageIndex: Int): String? =
        withContext(Dispatchers.IO) {
            val f = File(context.filesDir, getFileName(noteId, pageIndex))
            return@withContext if (f.exists()) {
                f.readText()
            } else {
                null
            }
        }
}
