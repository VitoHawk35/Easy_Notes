package com.easynote.data.test

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
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
            Log.d("ViewModel", "Created new note with ID: $pagingData")
        }
    }
}
