package com.easynote.detail.data.model

/**
 * 整个笔记的数据模型
 * @param id 笔记唯一标识
 * @param title 笔记标题
 * @param createTime 创建时间
 * @param pages 包含的页列表
 * @param tags 标签列表
 * @param isPinned 是否置顶
 */
data class Note(
    val id: Long,
    var title: String,
    var createTime: Long,
    var pages: MutableList<NotePage> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),
    var isPinned: Boolean = false
)