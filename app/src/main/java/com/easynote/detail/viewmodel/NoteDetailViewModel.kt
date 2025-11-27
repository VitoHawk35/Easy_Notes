package com.easynote.detail.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.easynote.detail.data.model.NotePage
import com.example.mydemo.data.entity.NoteEntity
import com.example.mydemo.data.repository.impl.NoteRepositoryImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}