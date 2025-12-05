package com.easynote.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "note_fts")
data class NoteFts(

    @ColumnInfo
    var id:Long,

    @ColumnInfo(name = "page_index")
    var pageIndex: Int,

    @ColumnInfo
    var title: String? = null,

    @ColumnInfo
    var summary: String? = null,

    @ColumnInfo
    var content: String? = null

)