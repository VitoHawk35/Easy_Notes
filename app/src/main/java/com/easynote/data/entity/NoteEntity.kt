package com.easynote.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo
    var title: String? = null,

    @ColumnInfo
    var content: String? = null,

    @ColumnInfo
    var abstract: String? = null,

    @ColumnInfo(name = "create_time")
    var createTime: Long? = null,

    @ColumnInfo(name = "update_time")
    var updateTime: Long? = null,

    @ColumnInfo(name = "favorite_time")
    var favoriteTime: Long? = null,

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean? = null
)