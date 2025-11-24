package com.example.mydemo.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mydemo.data.dao.NoteEntityDao;
import com.example.mydemo.data.dao.TagEntityDao;
import com.example.mydemo.data.entity.NoteEntity;
import com.example.mydemo.data.entity.TagEntity;

@Database(entities = {NoteEntity.class, TagEntity.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase instance;
    public synchronized static NoteDatabase getInstance(Context context) {
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),NoteDatabase.class,"note_database")
                    .build();
        }
        return instance;
    }

    public abstract NoteEntityDao getNoteEntityDao();

    public abstract TagEntityDao getTagEntityDao();


}
