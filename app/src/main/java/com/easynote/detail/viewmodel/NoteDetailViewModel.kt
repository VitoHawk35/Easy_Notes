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
import androidx.core.text.parseAsHtml

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RepositoryImpl(application)

    val notePages = MutableLiveData<List<NotePage>>()
    val isLoading = MutableLiveData<Boolean>()
    val noteTags = MutableLiveData<List<TagEntity>>()
    val noteTitle = MutableLiveData<String>()
    val allTagsFlow: Flow<PagingData<TagEntity>> = repository.getAllTagsFlow(20).cachedIn(viewModelScope)


    var currentTitle: String = ""
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



    fun saveNotePage(noteId: Long, pageIndex: Int, htmlContent: String) {
        viewModelScope.launch {
            try {
                // 提取纯文本用于搜索预览
                val plainText = htmlContent.parseAsHtml().toString().trim()

                // 调用 Repository 保存
                repository.updateNoteContent(
                    noteId = noteId,
                    pageIndex = pageIndex,
                    newContent = plainText,
                    newHTMLContent = htmlContent
                )
                repository.updateTitleOrSummary(noteId, currentTitle, null)
                Log.d("NoteDetailViewModel", "第 $pageIndex 页保存成功,标题已更新: $currentTitle")
                //自动生成摘要
                generateSummaryAuto(noteId, plainText)
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
    fun updateAbstract(noteId: Long, abstract: String) {
        viewModelScope.launch {
            try {
                repository.updateTitleOrSummary(noteId, null,abstract)
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

    /**
     * 后台静默调用 AI 生成摘要
     */
    private fun generateSummaryAuto(noteId: Long, content: String) {
        // 调用 AIProvider 发起摘要任务
        AIProvider.getInstance().process(content, TaskType.SUMMARY, object : AIResultCallback {
            override fun onSuccess(aiReply: String) {
                // AI 成功返回后，调用 updateAbstract 更新到数据库
                updateAbstract(noteId, aiReply)
                Log.d("NoteDetailViewModel", "AI 自动摘要已更新: $aiReply")
            }

            override fun onFailure(e: AIException) {
                // AI 失败仅记录日志
                Log.e("NoteDetailViewModel", "AI 自动摘要生成失败: ${e.message}")
            }
        })
    }

    fun saveNote(noteId: Long, pages: List<NotePage>, tags: List<TagEntity>) {
        viewModelScope.launch {
            try {
                val titleToSave = if (currentTitle.isBlank()) "无标题笔记" else currentTitle

                val firstPageHtml = pages.firstOrNull()?.content ?: ""
                val summary = firstPageHtml.parseAsHtml().toString().trim()

                repository.updateTitleOrSummary(noteId, titleToSave, summary)

                repository.updateNoteTags(noteId, *tags.toTypedArray())

                pages.forEach { page ->
                    val plainText = page.content.parseAsHtml().toString().trim()
                    repository.updateNoteContent(
                        noteId = noteId,
                        pageIndex = page.pageNumber,
                        newContent = plainText,
                        newHTMLContent = page.content
                    )
                    //
                    generateSummaryAuto(noteId, plainText)
                }

                Log.d("NoteDetailViewModel", "笔记(ID=$noteId) 已全部保存/更新")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NoteDetailViewModel", "保存失败: ${e.message}")
            }
        }
    }

}