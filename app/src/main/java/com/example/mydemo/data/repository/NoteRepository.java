package com.example.mydemo.data.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.example.mydemo.data.entity.NoteEntity;
import com.example.mydemo.data.entity.TagEntity;

import java.util.List;

public interface NoteRepository {

    /**
     * Insert a new note.
     * @param noteEntity
     */
    void insertNote(NoteEntity... noteEntity);

    /**
     * Delete a note.
     * @param noteEntity
     */
    void deleteNote(NoteEntity... noteEntity);

    /**
     * Delete a note by its ID.
     * @param id
     */
    void deleteNoteById(int id);

    /**
     * Update a note.
     * @param noteEntity
     */
    void updateNote(NoteEntity... noteEntity);

    /**
     * Insert a new tag.
     * @param tagEntity
     */
    void insertTag(TagEntity... tagEntity);

    /**
     * Delete a tag.
     * @param tagEntity
     */
    void deleteTag(TagEntity... tagEntity);

    /**
     * Delete a tag by its ID.
     * @param id
     */
    void deleteTagById(int id);

    /**
     * Update tag.
     * @param tagEntity
     */
    void updateTag(TagEntity... tagEntity);

    /**
     * Get all notes as LiveData.
     * @return LiveData<List<NoteEntity>>
     */
    LiveData<NoteEntity> getNoteByIdLive(int id);

    /**
     * Get all notes as LiveData.
     * @return LiveData<List<NoteEntity>>
     */
    LiveData<List<NoteEntity>> getAllNotesLive();

    /**
     * Get a tag by its ID as LiveData.
     * @param id
     * @return LiveData<TagEntity>
     */
    LiveData<TagEntity> getTagByIdLive(int id);

    /**
     * Get all tags as LiveData.
     * @return LiveData<List<TagEntity>>
     */
    LiveData<List<TagEntity>> getAllTagsLive();

    /**
     * Get all notes with pagination support.
     * @return LiveData<PagingData<NoteEntity>>
     */
    LiveData<PagingData<NoteEntity>> getAllNotesPaging(int pageSize);

    /**
     * Get all tags with pagination support.
     * @return LiveData<PagingData<TagEntity>>
     */
    LiveData<PagingData<TagEntity>> getAllTagsPaging(int pageSize);

}
