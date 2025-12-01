package com.example.mydemo.data.repository.impl

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mydemo.common.exception.DataException
import com.example.mydemo.constants.DataExceptionConstants
import com.example.mydemo.data.dao.TagEntityDao
import com.example.mydemo.data.database.NoteDatabase
import com.example.mydemo.data.entity.TagEntity
import com.example.mydemo.data.relation.TagWithNotes
import com.example.mydemo.data.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TagRepositoryImpl(application: Application) : TagRepository {
    private val tagEntityDao: TagEntityDao

    init {
        val noteDatabase = NoteDatabase.getInstance(application)
        this.tagEntityDao = noteDatabase.getTagEntityDao()
    }

    override fun getPagingTagsFlow(pageSize: Int): Flow<PagingData<TagEntity>> {
        val pager = Pager<Int, TagEntity>(
            PagingConfig(
                pageSize,
                pageSize,
                false,
                pageSize * 2
            )
        ) { tagEntityDao.getPagingTags() }
        return pager.flow
    }

    override fun getAllTagsFlow(): Flow<List<TagEntity>> {
        return tagEntityDao.getAllFlow()
    }

    override suspend fun insertTag(tagEntity: TagEntity): Long {
        try {
            return tagEntityDao.insert(tagEntity)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_INSERT_DATA_FAILED)
        }
    }

    override suspend fun deleteTag(vararg tagEntity: TagEntity) = withContext(Dispatchers.IO) {
        try {
            tagEntityDao.delete(*tagEntity)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED)
        }
    }

    override suspend fun deleteTagById(id: Int) = withContext(Dispatchers.IO) {
        try {
            tagEntityDao.deleteById(id)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED)
        }
    }

    override suspend fun updateTag(vararg tagEntity: TagEntity) = withContext(Dispatchers.IO) {
        try {
            tagEntityDao.update(*tagEntity)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED)
        }
    }

    override fun getTagByIdLive(id: Int): LiveData<TagEntity> {
        TODO("Not yet implemented")
    }


}