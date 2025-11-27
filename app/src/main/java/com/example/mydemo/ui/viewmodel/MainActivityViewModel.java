package com.example.mydemo.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mydemo.data.entity.NoteEntity;
import com.example.mydemo.data.entity.TagEntity;
import com.example.mydemo.data.repository.NoteRepository;
import com.example.mydemo.data.repository.impl.NoteRepositoryImpl;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;

    private LiveData<List<NoteEntity>> notes;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepositoryImpl(application);
    }

    public void setTitleAndContent(String title, String content) {
        NoteEntity noteEntity = new NoteEntity();
        noteEntity.setTitle(title);
        noteEntity.setContent(content);
        noteRepository.insertNote(noteEntity);
    }

    public void setTagName(String tagName, String color) {
        color = color == null ? "#FFFFFF" : color;
        noteRepository.insertTag(new TagEntity(tagName, color));
    }

    public LiveData<List<NoteEntity>> getNotes(){
        notes = noteRepository.getAllNotesLive();
        return notes;
    }

    public LiveData<List<TagEntity>> getTags() {
        return noteRepository.getAllTagsLive();
    }

    public LiveData<NoteEntity> getNoteById(int id){
        return noteRepository.getNoteByIdLive(id);
    }

    public void updateNote(NoteEntity noteEntity){
        noteRepository.updateNote(noteEntity);
    }

    public void deleteNote(NoteEntity note) {
        noteRepository.deleteNote(note);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(noteRepository instanceof NoteRepositoryImpl){
            ((NoteRepositoryImpl) noteRepository).closeExecutorService();
        }
    }
}
