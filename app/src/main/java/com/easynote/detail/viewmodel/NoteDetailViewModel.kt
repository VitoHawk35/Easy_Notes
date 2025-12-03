package com.easynote.detail.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.impl.RepositoryImpl
import com.easynote.detail.data.model.NotePage
import com.easynote.ai.core.AIProvider
import com.easynote.ai.core.TaskType
import com.easynote.data.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.easynote.ai.core.AIResultCallback
import com.easynote.ai.exception.AIException
import kotlinx.coroutines.flow.Flow
import java.io.File

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
            //修改为1
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
//        fun getNoteById(id: Int): LiveData<NoteEntity> {
//            return repository.getNoteByIdLive(id)
//        }




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


            } catch (e: Exception) {
                e.printStackTrace()
                // 通知 Activity "保存失败"
            }
        }
    }

    // 图片保存
    fun saveImage(noteId: Long, pageIndex: Int, sourceUri: Uri, onResult: (Uri) -> Unit) {
        viewModelScope.launch {
            try {
                val localPath = repository.saveImage(noteId, pageIndex, sourceUri)
                onResult(Uri.fromFile(File(localPath)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 更新笔记摘要
    fun updateAbstract(noteId: Long, noteTitle:String, abstract: String) {
        viewModelScope.launch {
            try {
                repository.updateTitleOrTitle(noteId, noteTitle,abstract)
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