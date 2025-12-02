package com.easynote.detail.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import com.easynote.detail.data.model.NotePage
import com.example.mydemo.ai.core.AIProvider
import com.example.mydemo.ai.core.TaskType
import com.example.mydemo.ai.model.Response.ChatCompletionResponse
import com.easynote.data.entity.NoteEntity
import com.easynote.data.repository.impl.NoteRepositoryImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepositoryImpl(application)
    private val gson = Gson()

    private val _saveState = MutableLiveData<Boolean>()
    val saveState: LiveData<Boolean> = _saveState

    /**
     * 保存笔记逻辑
     * @param noteId: 数据库中的笔记ID (-1 表示新建)
     * @param title: 标题
     * @param pages: 页面列表
     */
    fun saveNote(noteId: Int, title: String, pages: List<NotePage>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentJson = gson.toJson(pages)

                val entity = NoteEntity().apply {
                    this.title = title
                    this.content = contentJson

                    if (noteId != -1) {
                        this.id = noteId
                    }
                }

                if (noteId == -1) {
                    repository.insertNote(entity)
                } else {
                    repository.updateNote(entity)
                }

                _saveState.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _saveState.postValue(false)
            }
        }
    }

    /**
     * 解析逻辑：把数据库读出来的 String 变回 List<NotePage>
     */
    fun parsePagesFromJson(json: String?): MutableList<NotePage> {
        if (json.isNullOrEmpty()) {
            return mutableListOf(NotePage(System.currentTimeMillis(), 1, ""))
        }
        return try {
            val type = object : TypeToken<List<NotePage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            mutableListOf(NotePage(System.currentTimeMillis(), 1, json))
        }
    }

    fun getNoteById(id: Int): LiveData<NoteEntity> {
        return repository.getNoteByIdLive(id)
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