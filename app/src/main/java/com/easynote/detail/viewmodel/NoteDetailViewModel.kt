package com.easynote.detail.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import com.easynote.detail.data.model.NotePage
import com.example.mydemo.ai.core.AIProvider
import com.example.mydemo.ai.core.TaskType
import com.example.mydemo.ai.model.Response.ChatCompletionResponse
import com.easynote.data.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RepositoryImpl(application)

    val notePages = MutableLiveData<List<NotePage>>()
    val isLoading = MutableLiveData<Boolean>()
    val noteTags = MutableLiveData<List<TagEntity>>()
    val noteTitle = MutableLiveData<String>()
    val allTagsFlow: Flow<PagingData<TagEntity>> = repository.getAllTagsFlow(20).cachedIn(viewModelScope)
    fun loadNoteContent(noteId: Long) {
        isLoading.value = true
        viewModelScope.launch {
            val noteWithTags = repository.getNoteWithTagsById(noteId)

            if (noteWithTags != null) {
                val title = noteWithTags.noteEntity?.title
                if (!title.isNullOrEmpty()) {
                    noteTitle.value = title!!
                }

                noteWithTags.tags?.let { tagEntities ->
                    noteTags.value = tagEntities
                }
            }

            val loadedPages = mutableListOf<NotePage>()
            var pageIndex = 1

            withContext(Dispatchers.IO) {
                while (true) {
                    val content = repository.getNoteContentByIdAndPageIndex(noteId, pageIndex)

                    if (content != null) {

                        loadedPages.add(NotePage(System.currentTimeMillis() + pageIndex, pageIndex, content))
                        pageIndex++
                    } else {
                        break
                    }
                }
            }

            if (loadedPages.isEmpty()) {
                loadedPages.add(NotePage(System.currentTimeMillis(), 1, ""))
            }

            notePages.value = loadedPages
            isLoading.value = false
        }
    }

    fun updateNoteTags(noteId: Long, tags: List<TagEntity>) {
        viewModelScope.launch {
            try {
                repository.updateNoteTags(noteId, *tags.toTypedArray())

                Log.d("NoteDetailViewModel", "笔记 $noteId 的标签保存成功，共 ${tags.size} 个")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NoteDetailViewModel", "保存标签失败: ${e.message}")
            }
        }
    }

    private val repository2: Repository = RepositoryImpl(application)

    fun saveNotePage(noteId: Long, pageIndex: Int, htmlContent: String) {
        viewModelScope.launch {
            try {
                // 提取纯文本用于搜索预览（可选，简单正则去标签）
                val plainText = htmlContent.replace(Regex("<[^>]*>"), "")

                // 调用 Repository 保存
                repository2.updateNoteContent(
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
                val localPath = repository2.saveImage(noteId, pageIndex, sourceUri)
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