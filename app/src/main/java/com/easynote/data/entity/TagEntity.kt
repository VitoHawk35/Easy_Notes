package com.easynote.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo
    var name: String? = "新建标签",

    @ColumnInfo
    var color: String? = "#FFFFFF"
)
