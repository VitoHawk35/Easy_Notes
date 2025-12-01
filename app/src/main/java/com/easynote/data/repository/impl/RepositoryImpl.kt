package com.easynote.data.repository.impl

import android.app.Application
import android.content.Context
import androidx.paging.PagingData
import androidx.room.Transaction
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
import com.easynote.data.repository.FileRepository
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.Repository
import com.easynote.data.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import androidx.core.content.edit
import com.easynote.data.annotation.NoteOrderWay
import com.easynote.data.annotation.ORDER_UPDATE_TIME_DESC
import com.easynote.data.entity.NoteEntity
import com.easynote.data.relation.TagWithNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RepositoryImpl(application: Application) : Repository {
    private val noteRepository: NoteRepository
    private val fileRepository: FileRepository
    private val tagRepository: TagRepository

    init {
        this.noteRepository = NoteRepositoryImpl(application)
        this.fileRepository = FileRepositoryImpl(application)
        this.tagRepository = TagRepositoryImpl(application)

    }

    override suspend fun createNewNote(noteWithTags: NoteWithTags?): Long {
        return noteRepository.insertNoteWithTags(noteWithTags ?: NoteWithTags())
    }

    override suspend fun createNewTag(tagEntity: TagEntity): Long {
        return tagRepository.insertTag(tagEntity)
    }

    @Transaction
    override suspend fun deleteNoteById(noteId: Long) {
        noteRepository.deleteNoteById(noteId)
        fileRepository.deleteFile(noteId)
    }

    override suspend fun deleteNotePage(noteId: Long, pageIndex: Int) {

        fileRepository.deletePage(noteId, pageIndex)

    }

    @Transaction
    override suspend fun deleteTagSafelyById(tagId: Long): Boolean {
        val tagWithNotes: TagWithNotes = tagRepository.getTagWithNotesById(tagId)
        return if (tagWithNotes.notes.isNullOrEmpty()) {
            tagRepository.deleteTagById(tagId)
            true
        } else {
            false
        }
    }

    override suspend fun saveImage(
        noteId: Long,
        pageIndex: Int,
        imgPath: String
    ): String {
        return fileRepository.saveImage(noteId, pageIndex, imgPath)
    }

    @Transaction
    override suspend fun updateNoteContent(
        noteId: Long,
        pageIndex: Int,
        newContent: String,
        newHTMLContent: String
    ) {
        fileRepository.updateFile(noteId, pageIndex, newContent, newHTMLContent)
        noteRepository.updateAbstract(noteId, newContent.take(200))
        noteRepository.updateNoteUpdateTime(noteId)
    }

    override suspend fun updateAbstract(noteId: Long, abstract: String) {
        noteRepository.updateAbstract(noteId, abstract)
    }

    override suspend fun updateNoteTags(
        noteId: Long,
        vararg tagEntities: TagEntity
    ) {
        noteRepository.updateNoteTags(noteId, *tagEntities)
    }

    override fun getAllNoteWithTagsPagingFlow(
        pageSize: Int,
        @NoteOrderWay orderWay: String?
    ): Flow<PagingData<NoteWithTags>> {
        return noteRepository.getAllNoteWithTagsPagingFlow(
            pageSize,
            orderWay ?: ORDER_UPDATE_TIME_DESC
        )
    }

    override fun getNoteByTags(
        tagIds: Set<Long>?,
        pageSize: Int,
        orderWay: String?
    ): Flow<PagingData<NoteEntity>> {
        return noteRepository.getNoteByTagIdPagingFlow(
            tagIds,
            pageSize,
            orderWay ?: ORDER_UPDATE_TIME_DESC
        )
    }

    override fun getAllTagsFlow(pageSize: Int): Flow<PagingData<TagEntity>> {
        return tagRepository.getPagingTagsFlow(pageSize)
    }

    override suspend fun getNoteContentByIdAndPageIndex(
        noteId: Long,
        pageIndex: Int
    ): String? {
        return fileRepository.readH5File(noteId, pageIndex)
    }


    override suspend fun searchNotesByQuery(
        query: String,
        pageSize: Int
    ): Flow<PagingData<NoteWithTags>> {
        return noteRepository.searchNotesByQueryFlow(query, pageSize)
    }

    override suspend fun modifyOrderWay(context: Context, way: String) =
        withContext(Dispatchers.IO) {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit {
                    putString("note_order_way", way)
                }
        }

    override suspend fun getOrderWay(context: Context): String =
        withContext(Dispatchers.IO) {
            val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            p.getString("note_order_way", "update_time_desc") ?: "update_time_desc"
        }

    override suspend fun updateNoteFavorite(noteId: Long, isFavour: Boolean) {
        TODO("Not yet implemented")
    }
}