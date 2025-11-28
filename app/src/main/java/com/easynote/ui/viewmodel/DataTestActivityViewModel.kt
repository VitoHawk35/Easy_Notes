package com.easynote.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.PagingData
import com.easynote.data.relation.NoteWithTags
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import kotlinx.coroutines.flow.Flow

class DataTestActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository


    init {
        this.repository = RepositoryImpl(application)


    }

    suspend fun getOrderWay(): String {
        return repository.getOrderWay(this.getApplication())
    }

    fun getFlow(): Flow<PagingData<NoteWithTags>> {
        return repository.getAllNoteWithTagsPagingFlow(20)
    }
}