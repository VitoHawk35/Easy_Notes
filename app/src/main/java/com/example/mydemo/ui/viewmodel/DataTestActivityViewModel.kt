package com.example.mydemo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mydemo.data.repository.Repository
import com.example.mydemo.data.repository.impl.RepositoryImpl

class DataTestActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    init {
        this.repository = RepositoryImpl(application)

    }

    suspend fun getOrderWay(): String {
        return repository.getOrderWay(this.getApplication())
    }
}