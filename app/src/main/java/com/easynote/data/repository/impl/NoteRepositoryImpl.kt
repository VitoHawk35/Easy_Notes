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
import com.easynote.data.annotation.UPDATE_TIME_DESC
import com.easynote.data.common.utils.ToPinyin
import com.easynote.data.dao.NoteFtsDao
import com.easynote.data.dao.NoteEntityDao
import com.easynote.data.dao.NoteTagCrossRefDao
import com.easynote.data.dao.TagEntityDao
import com.easynote.data.database.NoteDatabase
import com.easynote.data.entity.NoteEntity
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
import com.easynote.data.repository.FileRepository
import com.easynote.data.repository.NoteRepository
import com.github.promeg.pinyinhelper.Pinyin.toPinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(application: Application) : NoteRepository {
    private val noteEntityDao: NoteEntityDao
    private val tagEntityDao: TagEntityDao
    private val fileRepository: FileRepository
    private val noteTagRefDao: NoteTagCrossRefDao
    private val noteFtsDao: NoteFtsDao

    private val pinYinConverter: ToPinyin

    init {
        val noteDatabase = NoteDatabase.getInstance(application)
        this.noteEntityDao = noteDatabase.getNoteEntityDao()
        this.tagEntityDao = noteDatabase.getTagEntityDao()
        this.noteTagRefDao = noteDatabase.getNoteTagCrossRefDao()
        this.fileRepository = FileRepositoryImpl(application)
        this.noteFtsDao = noteDatabase.getNoteContentSearchDao()
        this.pinYinConverter = ToPinyin(application.applicationContext)
    }

    @Transaction
    override suspend fun insertNote(noteEntity: NoteEntity): Long =
        withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()

                noteEntity.createTime = now
                noteEntity.updateTime = now
                if (noteEntity.isFavorite == null) {
                    noteEntity.isFavorite = false
                }

                val id = noteEntityDao.insert(noteEntity)
                noteFtsDao.insert(id, 1)
                id
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
        val id = noteEntityDao.insert(
            noteWithTags.noteEntity ?: NoteEntity(
                createTime = now,
                updateTime = now
            )
        )
        noteTagRefDao.insertNoteWithTags(
            id,
            noteWithTags.tags?.mapNotNull { it.id } ?: emptyList()
        )

        return@withContext id
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
                noteFtsDao.deleteByNoteId(id)
                fileRepository.deleteFile(id)
            } catch (e: Exception) {
                throw DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED)
            }
        }

    @Transaction
    override suspend fun deleteNoteById(id: Set<Long>) =
        withContext(Dispatchers.IO) {
            noteEntityDao.deleteById(id)
            noteTagRefDao.deleteCrossRefsByNoteId(id)
            noteFtsDao.deleteByNoteId(id)
        }

    override suspend fun deleteNotePage(noteId: Long, pageIndex: Int) {
        noteFtsDao.deleteByNoteIdAndPageIndex(noteId, pageIndex)
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

    override suspend fun updateNoteFavor(id: Long, isFavor: Boolean) {
        try {
            noteEntityDao.updateFavor(id, isFavor)
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED)
        }
    }

    override suspend fun updateNoteFavor(
        id: Set<Long>,
        isFavor: Boolean
    ) = withContext(Dispatchers.IO) {
        noteEntityDao.updateFavor(id, isFavor)
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

    override suspend fun updateTitleOrSummary(noteId: Long, title: String?, summary: String?) =
        withContext(Dispatchers.IO) {
            noteEntityDao.updateTitleOrSummary(noteId, title, summary, System.currentTimeMillis())
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
        withContext(Dispatchers.IO) {
            noteEntityDao.getWithTags(id)
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
            noteEntityDao.getPagingByTagIds(TagIds, orderWay ?: UPDATE_TIME_DESC)
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
                pinYinConverter.convertToPinyin(query),
                startTime,
                endTime,
                orderWay ?: UPDATE_TIME_DESC,
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

    @Transaction
    override suspend fun updateSearchTable(
        noteId: Long,
        pageIndex: Int?,
        title: String?,
        summary: String?,
        content: String?
    ) = withContext(Dispatchers.IO) {
        if (noteFtsDao.getNoteFtsId(noteId, pageIndex) == null) {
            noteFtsDao.insert(
                noteId,
                pageIndex,
                pinYinConverter.convertToPinyin(title),
                pinYinConverter.convertToPinyin(summary),
                pinYinConverter.convertToPinyin(content),
            )
        } else {
            noteFtsDao.update(
                noteId,
                pageIndex,
                title = pinYinConverter.convertToPinyin(title),
                summary = pinYinConverter.convertToPinyin(summary),
                content = pinYinConverter.convertToPinyin(content),
            )
        }
    }

    override fun getAllNoteFlow(
        query: String?,
        tagIds: Set<Long>?,
        startTime: Long?,
        endTime: Long?,
        orderWay: String?
    ): Flow<List<NoteWithTags>> {
        try {
            return noteEntityDao.getAllWithTags(
                query,
                tagIds,
                startTime,
                endTime,
                tagIds?.size ?: 0
            )
        } catch (e: Exception) {
            throw DataException(e, DataExceptionConstants.DB_QUERY_DATA_FAILED)
        }
    }
}