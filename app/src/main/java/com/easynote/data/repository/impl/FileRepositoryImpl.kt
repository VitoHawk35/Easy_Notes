package com.easynote.data.repository.impl

import android.app.Application
import android.content.Context
import com.easynote.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepositoryImpl(application: Application) : FileRepository {
    private val context: Context = application.applicationContext

    private fun getH5FileName(noteId: Long, pageIndex: Int): String {
        return "${noteId}/${pageIndex}/H5.html"
    }

    private fun getTxtFileName(noteId: Long, pageIndex: Int): String {
        return "${noteId}/${pageIndex}/content.txt"
    }

    override suspend fun insertFile(
        noteId: Long,
        pageIndex: Int,
        content: String,
        htmlContent: String,
    ) = withContext(
        Dispatchers.IO
    ) {
        File(context.filesDir, getTxtFileName(noteId, pageIndex)).apply {
            parentFile?.mkdirs()
            writeText(content)
        }
        File(context.filesDir, getH5FileName(noteId, pageIndex)).apply {
            parentFile?.mkdirs()
            writeText(htmlContent)
        }

        val imgDir = File(context.filesDir, "$noteId/$pageIndex/img")
        if (!imgDir.exists()) {
            imgDir.mkdirs()
        }

        return@withContext
    }

    override suspend fun saveImage(
        noteId: Long,
        pageIndex: Int,
        imgPath: String
    ): String = withContext(Dispatchers.IO) {
        val imgDir = File(context.filesDir, "$noteId/$pageIndex/img")
        if (!imgDir.exists()) {
            imgDir.mkdirs()
        }

        val src = File(imgPath)

        val dst = File(imgDir, src.name)
        src.copyTo(dst, overwrite = true)

        dst.absolutePath
    }

    override suspend fun deletePage(noteId: Long, pageIndex: Int) =
        withContext(Dispatchers.IO) {
            var dir = File(context.filesDir, "${noteId}/${pageIndex}")
            if (!dir.exists() || !dir.isDirectory) {
                return@withContext
            }
            if (dir.exists()) {
                dir.delete()
            }
            dir = File(context.filesDir, noteId.toString())
            val dirsToShift = dir.listFiles { file ->
                file.isDirectory
            }?.mapNotNull { subDir ->
                val index = subDir.name.toIntOrNull()
                index?.let { it to subDir }
            }?.filter { (index, _) ->
                index > pageIndex
            }?.sortedBy { (index, _) ->
                index
            } ?: emptyList()

            for ((index, subDir) in dirsToShift) {
                val newIndex = index - 1
                val newDir = File(dir, newIndex.toString())
                if (!newDir.exists()) {
                    subDir.renameTo(newDir)
                }
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
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateFile(
        noteId: Long,
        pageIndex: Int,
        content: String,
        htmlContent: String,
    ) = withContext(Dispatchers.IO) {
        File(context.filesDir, getTxtFileName(noteId, pageIndex)).apply {
            parentFile?.mkdirs()
            writeText(content)
        }
        File(context.filesDir, getH5FileName(noteId, pageIndex)).apply {
            parentFile?.mkdirs()
            writeText(htmlContent)
        }

        return@withContext
    }

    override suspend fun readH5File(noteId: Long, pageIndex: Int): String? =
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, getH5FileName(noteId, pageIndex))
            return@withContext if (file.exists()) {
                file.readText()
            } else {
                null
            }
        }

    override suspend fun readTxtFile(noteId: Long, pageIndex: Int): String? =
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, getTxtFileName(noteId, pageIndex))
            return@withContext if (file.exists()) {
                file.readText()
            } else {
                null
            }
        }
}
