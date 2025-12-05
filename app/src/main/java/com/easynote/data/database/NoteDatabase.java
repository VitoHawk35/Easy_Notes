package com.easynote.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.easynote.data.dao.NoteFtsDao;
import com.easynote.data.dao.NoteEntityDao;
import com.easynote.data.dao.NoteTagCrossRefDao;
import com.easynote.data.dao.TagEntityDao;
import com.easynote.data.entity.NoteFts;
import com.easynote.data.entity.NoteEntity;
import com.easynote.data.entity.NoteTagCrossRef;
import com.easynote.data.entity.TagEntity;

@Database(entities = {NoteEntity.class, TagEntity.class, NoteTagCrossRef.class, NoteFts.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase instance;

    public synchronized static NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class, "note_database")
                    .createFromAsset("note_database.db")
                    .addCallback(new RoomDatabase.Callback(){
                        @Override
                        public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            db.execSQL("INSERT INTO note_fts(id) select id from note");
                        }
                    })
                    .build();
        }
        return instance;
    }

    public abstract NoteEntityDao getNoteEntityDao();

    public abstract TagEntityDao getTagEntityDao();

    public abstract NoteTagCrossRefDao getNoteTagCrossRefDao();

    public abstract NoteFtsDao getNoteContentSearchDao();


}
