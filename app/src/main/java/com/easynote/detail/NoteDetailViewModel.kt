package com.easynote.detail

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import com.example.mydemo.ai.core.AIProvider
import com.example.mydemo.ai.core.TaskType
import com.example.mydemo.ai.model.Response.ChatCompletionResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.net.toUri

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository = RepositoryImpl(application)

    fun saveNotePage(noteId: Long, pageIndex: Int, htmlContent: String) {
        viewModelScope.launch {
            try {
                // 提取纯文本用于搜索预览（可选，简单正则去标签）
                val plainText = htmlContent.replace(Regex("<[^>]*>"), "")

                // 调用 Repository 保存
                repository.updateNoteContent(
                    noteId = noteId,
                    pageIndex = pageIndex,
                    newContent = plainText,
                    newHTMLContent = htmlContent
                )

                Log.d("NoteDetailViewModel", "第 $pageIndex 页保存成功")
                // 这里可以通过 LiveData/Flow 通知 Activity "保存成功" (可选)

            } catch (e: Exception) {
                e.printStackTrace()
                // 通知 Activity "保存失败"
            }
        }
    }

    // 3. 之前讨论的图片保存也可以搬到这里
    fun saveImage(noteId: Long, pageIndex: Int, sourceUri: Uri, onResult: (Uri) -> Unit) {
        viewModelScope.launch {
            try {
                val localPath = repository.saveImage(noteId, pageIndex, sourceUri)
                onResult(localPath.toUri())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 处理 AI 任务
    fun performAiTask(text: String, taskType: TaskType, onResult: (String) -> Unit, onError: (String) -> Unit) {
        // 这里调用 AIProvider
        // 注意：AIProvider 最好也注入进来，或者单例调用
        AIProvider.getInstance().process(text, taskType, object : Callback<ChatCompletionResponse> {
            override fun onResponse(call: Call<ChatCompletionResponse>, response: Response<ChatCompletionResponse>) {
                val result = response.body()?.choices?.firstOrNull()?.message?.content
                if (result != null) {
                    onResult(result) // 成功，回调结果
                } else {
                    onError("AI返回内容为空")
                }
            }

            override fun onFailure(call: Call<ChatCompletionResponse>?, t: Throwable) {
                onError("网络错误: ${t.message}")
            }
        })
    }
}