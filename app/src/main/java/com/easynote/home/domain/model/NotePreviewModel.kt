package com.easynote.home.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
//笔记model定义
data class NotePreviewModel(
    val noteId: Long,//笔记id
    val title: String,//笔记题目
    val summary: String,//笔记摘要
    val tagIds: Set<TagModel>,//笔记标签
    val createdTime: Long,//笔记创建时间
    val updatedTime: Long,//笔记更新时间
    val pinnedTime: Long,//笔记置顶时间
    val isPinned: Boolean//笔记是否置顶
) : Parcelable