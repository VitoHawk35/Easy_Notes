package com.easynote.data.repository.impl

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.room.Transaction
import com.easynote.data.common.exception.DataException
import com.easynote.data.common.constants.DataExceptionConstants
import com.easynote.data.annotation.NoteOrderWay
import com.easynote.data.annotation.ORDER_UPDATE_TIME_DESC
import com.easynote.data.dao.NoteContentSearchDao
import com.easynote.data.dao.NoteEntityDao
import com.easynote.data.dao.NoteTagCrossRefDao
import com.easynote.data.dao.TagEntityDao
import com.easynote.data.database.NoteDatabase
import com.easynote.data.entity.NoteEntity
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
import com.easynote.data.repository.FileRepository
import com.easynote.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(application: Application) : NoteRepository {
    private val noteEntityDao: NoteEntityDao
    private val tagEntityDao: TagEntityDao
    private val fileRepository: FileRepository
    private val noteTagRefDao: NoteTagCrossRefDao

    private val noteContentSearchDao: NoteContentSearchDao

    init {
        val noteDatabase = NoteDatabase.getInstance(application)
        this.noteEntityDao = noteDatabase.getNoteEntityDao()
        this.tagEntityDao = noteDatabase.getTagEntityDao()
        this.noteTagRefDao = noteDatabase.getNoteTagCrossRefDao()
        this.fileRepository = FileRepositoryImpl(application)
        this.noteContentSearchDao = noteDatabase.getNoteContentSearchDao()
    }

    override suspend fun insertNote(noteEntity: NoteEntity): Long =
        withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()

                noteEntity.createTime = now
                noteEntity.updateTime = now
                if (noteEntity.isFavorite == null) {
                    noteEntity.isFavorite = false
                }

                noteEntityDao.insert(noteEntity)
            } catch (e: Exception) {
                throw DataException(e, DataExceptionConstants.DB_INSERT_DATA_FAILED)
            }
        }

    @Transaction
    override suspend fun insertNoteWithTags(noteWithTags: NoteWithTags): Long = withContext(
        Dispatchers.IO
    ) {
        val now = System.currentTimeMillis()
        noteWithTags.noteEntity?.createTime = now
        noteWithTags.noteEntity?.updateTime = now
        if (noteWithTags.noteEntity?.isFavorite == null) {
            noteWithTags.noteEntity?.isFavorite = false
        }
        // TODO: 添加标签关联逻辑
        noteEntityDao.insert(noteWithTags.noteEntity ?: NoteEntity())
    }

    override suspend fun deleteNote(vararg noteEntity: NoteEntity): Int =
        withContext(Dispatchers.IO) {
            try {
                noteEntityDao.delete(*noteEntity)
            } catch (e: Exception) {
                throw DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED)
            }
        }

    @Transaction
    override suspend fun deleteNoteById(id: Long) =
        withContext(Dispatchers.IO) {
            try {
                noteEntityDao.deleteById(id)
                noteTagRefDao.deleteCrossRefsByNoteId(id)
                noteContentSearchDao.deleteByNoteId(id)
                fileRepository.deleteFile(id)
            } catch (e: Exception) {
                throw DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED)
            }
        }

    override suspend fun deleteNotePage(noteId: Long, pageIndex: Int) {
        noteContentSearchDao.deleteByNoteIdAndPageIndex(noteId, pageIndex)
    }

    override suspend fun updateNote(vararg noteEntity: NoteEntity) {
        try {
            for (n in noteEntity) {
                n.updateTime = System.currentTimeMillis()
            }
            noteEntityDao.update(*noteEntity)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED)
        }
    }

    override suspend fun updateNoteFavor(id: Int, isFavor: Boolean) {
        try {
            noteEntityDao.updateFavor(id, isFavor)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED)
        }
    }

    override suspend fun updateNoteTags(
        id: Long,
        vararg tagEntity: TagEntity
    ) = withContext(Dispatchers.IO) {
        val list: MutableList<Long> = mutableListOf()
        for (tag in tagEntity) {
            if (tag.id != null) {
                list.add(tag.id!!)
            }
        }

        noteTagRefDao.updateNoteTags(id, list)
    }

    override suspend fun updateAbstract(noteId: Long, abstract: String) =
        withContext(Dispatchers.IO) {
            noteEntityDao.updateAbstract(noteId, abstract, System.currentTimeMillis())
        }

    override suspend fun getAllNotes(): List<NoteEntity> =
        withContext(Dispatchers.IO) {
            try {
                noteEntityDao.getAll()
            } catch (e: Exception) {
                throw DataException(e, DataExceptionConstants.DB_QUERY_DATA_FAILED)
            }
        }

    override suspend fun getNoteById(id: Long): NoteWithTags? =
        withContext(Dispatchers.IO){ noteEntityDao.getWithTags(id)
    }

    override fun getNoteByTagIdPagingFlow(
        TagIds: Set<Long>?,
        pageSize: Int,
        orderWay: String?
    ): Flow<PagingData<NoteEntity>> {
        val pager = Pager<Int, NoteEntity>(
            PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize * 2
            )
        ) {
            noteEntityDao.getPagingByTagIds(TagIds, orderWay ?: ORDER_UPDATE_TIME_DESC)
        }
        return pager.flow
    }

    override fun getNoteByIdLive(id: Int): LiveData<NoteEntity> {
        return noteEntityDao.getLiveById(id)
    }

    override fun getAllNotesLive(): LiveData<MutableList<NoteEntity>> {
        return noteEntityDao.getAllLive()
    }

    override fun getAllNotesPaging(pageSize: Int): LiveData<PagingData<NoteEntity>> {
        val pager = Pager(
            PagingConfig(
                pageSize,
                pageSize,
                false,
                pageSize * 2
            )
        ) { noteEntityDao.getAllPaging() }
        return pager.liveData
    }

    override fun getAllNotePagingFlow(
        pageSize: Int,
        tagIds: Set<Long>?,
        query: String?,
        startTime: Long?,
        endTime: Long?,
        @NoteOrderWay orderWay: String?
    ): Flow<PagingData<NoteWithTags>> {
        val pager: Pager<Int, NoteWithTags> = Pager(
            PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize * 2
            )
        ) {
            noteEntityDao.getAllWithTagsPaging(
                tagIds,
                query,
                startTime,
                endTime,
                orderWay ?: ORDER_UPDATE_TIME_DESC,
                tagIds?.size ?: 0
            )
        }
        return pager.flow
    }

    override fun searchNotesByQueryFlow(
        query: String,
        pageSize: Int
    ): Flow<PagingData<NoteWithTags>> {
        val pager: Pager<Int, NoteWithTags> = Pager(
            PagingConfig(
                pageSize,
                pageSize,
                false,
                pageSize * 2
            )
        ) {
            noteEntityDao.searchNotesByAbstractFlow(query)
        }
        return pager.flow
    }

    override suspend fun updateNoteUpdateTime(noteId: Long) {
        noteEntityDao.updateUpdateTime(noteId, System.currentTimeMillis())
    }

    override suspend fun getNoteCountByTags(tagIds: Set<Long>): Int =
        withContext(Dispatchers.IO) {
            noteEntityDao.getCountByTagIds(tagIds)
        }

    override suspend fun updateSearchTable(noteId: Long, pageIndex: Int, take: String) =
        withContext(Dispatchers.IO) {
            noteContentSearchDao.insert(noteId, pageIndex, take)
        }
}
