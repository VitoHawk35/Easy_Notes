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
import com.easynote.ai.core.AIProvider
import com.easynote.ai.core.TaskType
import com.easynote.ai.model.Response.ChatCompletionResponse
import com.easynote.data.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.easynote.ai.core.AIResultCallback
import com.easynote.ai.exception.AIException
import kotlinx.coroutines.flow.Flow

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RepositoryImpl(application)

    val notePages = MutableLiveData<List<NotePage>>()
    val saveResult = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()
    val allTagsFlow: Flow<PagingData<TagEntity>> = repository.getAllTagsFlow(20).cachedIn(viewModelScope)
    fun loadNoteContent(noteId: Long) {
        isLoading.value = true
        viewModelScope.launch {
            val loadedPages = mutableListOf<NotePage>()
            var pageIndex = 0

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
//        fun getNoteById(id: Int): LiveData<NoteEntity> {
//            return repository.getNoteByIdLive(id)
//        }


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
    // 更新笔记摘要
    fun updateAbstract(noteId: Long, abstract: String) {
        viewModelScope.launch {
            try {
                repository.updateAbstract(noteId, abstract)
                Log.d("NoteDetailViewModel", "摘要更新成功")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NoteDetailViewModel", "摘要更新失败: ${e.message}")
            }
        }
    }

    // 处理 AI 任务
    fun performAiTask(text: String, taskType: TaskType, onResult: (String) -> Unit, onError: (String) -> Unit) {
        AIProvider.getInstance().process(text, taskType, object : AIResultCallback {
            override fun onSuccess(aiReply: String) {
                onResult(aiReply)
            }

            override fun onFailure(e: AIException) {
                onError("AI请求失败: ${e.message}")
            }
        })
    }

    // 翻译
    fun performTranslateTask(context: String, text: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        AIProvider.getInstance().processTranslate(context, text, object : AIResultCallback {
            override fun onSuccess(aiReply: String) {
                onResult(aiReply)
            }

            override fun onFailure(e: AIException) {
                onError("翻译请求失败: ${e.message}")
            }
        })
    }

}