package com.easynote.data.repository.impl

import android.app.Application
import android.content.Context
import android.net.Uri
import com.easynote.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

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
        imgPath: Uri
    ): String = withContext(Dispatchers.IO) {
        try {
            // 1. 检查是否已经是本地应用私有目录的文件 (避免重复拷贝)
            if (imgPath.scheme == "file" && imgPath.path?.contains(context.packageName) == true) {
                return@withContext imgPath.toString()
            }

            // 2. 规划存储路径：filesDir/noteId/pageIndex/img/
            val imgDir = File(context.filesDir, "$noteId/$pageIndex/img")
            if (!imgDir.exists()) {
                imgDir.mkdirs()
            }

            // 3. 生成唯一文件名
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val destFile = File(imgDir, fileName)

            // 4. 执行流拷贝
            // 使用 ContentResolver 打开输入流，这是读取 Uri 的标准方式
            val inputStream = context.contentResolver.openInputStream(imgPath)
                ?: throw Exception("无法打开图片流: $imgPath")

            inputStream.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // 5. 返回 file:// 格式的绝对路径
            return@withContext "file://${destFile.absolutePath}"

        } catch (e: Exception) {
            e.printStackTrace()
            // 出错时返回空字符串，或者根据你的需求抛出异常
            return@withContext ""
        }
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
