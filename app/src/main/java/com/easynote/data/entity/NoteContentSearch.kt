package com.easynote.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "note_content_search")
data class NoteContentSearch(

    @ColumnInfo(name = "page_index")
    var pageIndex: Int,

    @ColumnInfo
    var content: String? = null


)