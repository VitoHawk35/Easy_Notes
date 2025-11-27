package com.example.mydemo.data.repository.impl;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.example.mydemo.common.exception.DataException;
import com.example.mydemo.constants.DataExceptionConstants;
import com.example.mydemo.data.dao.NoteEntityDao;
import com.example.mydemo.data.dao.TagEntityDao;
import com.example.mydemo.data.database.NoteDatabase;
import com.example.mydemo.data.entity.NoteEntity;
import com.example.mydemo.data.entity.TagEntity;
import com.example.mydemo.data.repository.NoteRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepositoryImpl implements NoteRepository {

    private final NoteEntityDao noteEntityDao;
    private final TagEntityDao tagEntityDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NoteRepositoryImpl(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getInstance(application);
        this.noteEntityDao = noteDatabase.getNoteEntityDao();
        this.tagEntityDao = noteDatabase.getTagEntityDao();
    }

    public void closeExecutorService() {
        executorService.shutdown();
    }
    @Override
    public void insertNote(NoteEntity... noteEntity) {
        executorService.execute(() -> {
            try {
                if (noteEntity == null || noteEntity.length == 0) {
                    throw new DataException(DataExceptionConstants.INVALID_NOTE);
                }
                for (NoteEntity note : noteEntity) {
                    long now = System.currentTimeMillis();
                    note.setCreateTime(now);
                    note.setUpdateTime(now);
                }
                noteEntityDao.insert(noteEntity);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_INSERT_DATA_FAILED);
            }
        });
    }

    @Override
    public void deleteNote(NoteEntity... noteEntity) {
        executorService.execute(() -> {
            try {
                noteEntityDao.delete(noteEntity);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED);
            }
        });

    }

    @Override
    public void deleteNoteById(int id) {
        executorService.execute(() -> {
            try {
                noteEntityDao.deleteById(id);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED);
            }
        });
    }

    @Override
    public void updateNote(NoteEntity... noteEntity) {
        executorService.execute(() -> {
            try {
                for (NoteEntity n : noteEntity) {
                    n.setUpdateTime(System.currentTimeMillis());
                }
                noteEntityDao.update(noteEntity);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED);
            }
        });
    }


    @Override
    public void insertTag(TagEntity... tagEntity) {
        executorService.execute(() -> {
            try {
                tagEntityDao.insert(tagEntity);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_INSERT_DATA_FAILED);
            }
        });

    }

    @Override
    public void deleteTag(TagEntity... tagEntity) {
        executorService.execute(() -> {
            try {
                tagEntityDao.delete(tagEntity);
            } catch (Exception e) {
                throw new DataException(DataExceptionConstants.DB_DELETE_DATA_FAILED);
            }
        });

    }

    @Override
    public void deleteTagById(int id) {
        executorService.execute(() -> {
            try {
                tagEntityDao.deleteById(id);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_DELETE_DATA_FAILED);
            }
        });


    }

    @Override
    public void updateTag(TagEntity... tagEntity) {
        executorService.execute(() -> {
            try {
                tagEntityDao.update(tagEntity);
            } catch (Exception e) {
                throw new DataException(e, DataExceptionConstants.DB_UPDATE_DATA_FAILED);
            }
        });
    }

    @Override
    public LiveData<NoteEntity> getNoteByIdLive(int id) {
        return noteEntityDao.getLiveById(id);
    }

    @Override
    public LiveData<List<NoteEntity>> getAllNotesLive() {
        return noteEntityDao.getAllLive();
    }

    @Override
    public LiveData<TagEntity> getTagByIdLive(int id) {
        return tagEntityDao.getByIdLive(id);
    }

    @Override
    public LiveData<List<TagEntity>> getAllTagsLive() {
        return tagEntityDao.getAllLive();
    }

    @Override
    public LiveData<PagingData<NoteEntity>> getAllNotesPaging(int pageSize) {
        Pager<Integer, NoteEntity> pager = new Pager<>(
                new PagingConfig(
                        pageSize,
                        pageSize,
                        false,
                        pageSize * 2
                ),
                noteEntityDao::getAllPaging
        );
        return PagingLiveData.getLiveData(pager);
    }

    @Override
    public LiveData<PagingData<TagEntity>> getAllTagsPaging(int pageSize) {
        Pager<Integer, TagEntity> paper = new Pager<>(
                new PagingConfig(
                        pageSize,
                        pageSize,
                        false,
                        pageSize * 2
                ),
                tagEntityDao::getAllPaging
        );
        return null;
    }


}
