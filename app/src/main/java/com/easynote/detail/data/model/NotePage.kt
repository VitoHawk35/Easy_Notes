package com.easynote.detail.data.model

/**
 * 每一页笔记的数据模型
 * @param id 页面的唯一标识 (可以用时间戳模拟)
 * @param pageNumber 页码 (第几页)
 * @param content 这一页写的文字内容
 */
data class NotePage(
    val id: Long,
    var pageNumber: Int,
    var content: String
)