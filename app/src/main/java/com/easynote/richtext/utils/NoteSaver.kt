package com.easynote.richtext.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.regex.Pattern

object NoteSaver {

    /**
     * 保存笔记流程：
     * 纯粹的 I/O 操作，直接将 HTML 字符串写入文件。
     * 前置条件：HTML 中的图片路径必须已经在插入时被转换为 file:// 绝对路径。
     *
     * @param context 上下文
     * @param htmlContent 编辑器导出的 HTML
     * @param noteName 笔记的文件名 (例如 "my_note.html")
     */
    suspend fun saveNote(context: Context, htmlContent: String, noteName: String): String = withContext(Dispatchers.IO) {
        // 1. 确定保存目标文件
        val noteFile = File(context.filesDir, noteName)

        noteFile.writeText(htmlContent)

        Log.d("NoteSaver", "笔记保存成功: ${noteFile.absolutePath}")

        // 返回文件路径
        return@withContext noteFile.absolutePath
    }

}