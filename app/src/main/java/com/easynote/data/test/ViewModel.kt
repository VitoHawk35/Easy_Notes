package com.easynote.data.test

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    init {
        this.repository = RepositoryImpl(application)
    }

    fun click() {
        viewModelScope.launch {
            val id = repository.createNewNote()
            val pagingData = repository.getAllNoteWithTagsPagingFlow(20)
            pagingData.collectLatest { pagingData ->
                Log.d("ViewModel", "收到一页 PagingData: $pagingData")
                // 如果你的 item 有 toString，可这样粗略看一下：
                pagingData.toString()
            }

        }
    }
}
