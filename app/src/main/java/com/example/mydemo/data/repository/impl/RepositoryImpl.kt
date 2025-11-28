package com.example.mydemo.data.repository.impl

import android.app.Application
import android.content.Context
import androidx.paging.PagingData
import androidx.room.Transaction
import com.example.mydemo.data.entity.TagEntity
import com.example.mydemo.data.relation.NoteWithTags
import com.example.mydemo.data.repository.FileRepository
import com.example.mydemo.data.repository.NoteRepository
import com.example.mydemo.data.repository.Repository
import com.example.mydemo.data.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import androidx.core.content.edit
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

    @Transaction
    override suspend fun deleteNotePage(noteId: Long, pageIndex: Int) {
        if (pageIndex == 1) {
            fileRepository.readFile(noteId, 2)?.let {
                noteRepository.updateNoteContent(noteId, it)
                fileRepository.deleteFile(noteId, 2)
                return
            }
            noteRepository.updateNoteContent(noteId, "")
        } else {

            fileRepository.deleteFile(noteId, pageIndex)
        }
    }

    override suspend fun deleteTagSafelyById(tagId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateNoteContent(
        noteId: Long,
        pageIndex: Int,
        newContent: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateNoteTags(
        noteId: Long,
        tagEntities: List<TagEntity>
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllNoteWithTagsPagingFlow(pageSize: Int): Flow<PagingData<NoteWithTags>> {
        return noteRepository.getAllNoteWithTagsPagingFlow(pageSize)
    }

    override suspend fun getNoteContentByIdAndPageIndex(
        noteId: Long,
        pageIndex: Int
    ): String? {
        TODO("Not yet implemented")
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