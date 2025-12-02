package com.example.mydemo

import android.content.Context
import android.util.Log
import androidx.paging.LOG_TAG
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easynote.data.dao.NoteEntityDao
import com.easynote.data.database.NoteDatabase
import com.easynote.data.relation.NoteWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReadTest {
    private lateinit var noteEntityDao: NoteEntityDao
    private lateinit var noteDatabase: NoteDatabase

    @Before
    fun createDb() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        noteDatabase =
            Room.databaseBuilder(appContext, NoteDatabase::class.java, "note_database.db")
                .createFromAsset("note_database.db")
                .allowMainThreadQueries()
                .build()
        noteEntityDao = noteDatabase.getNoteEntityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        noteDatabase.close()
    }

    @Test
    @Throws(IOException::class)
    fun readTest() = runBlocking {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40,
                prefetchDistance = 20
            ),
            pagingSourceFactory = {
                noteEntityDao.getAllWithTagsPaging(
                    tagIds = null,
                    query = null,
                    startTime = null,
                    endTime = null,
                    orderWay = null,
                    tagSize = null
                )
            }
        )


    }

}