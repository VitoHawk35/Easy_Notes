package com.easynote.home.mapper // Or your data.mapper package

import com.easynote.data.entity.TagEntity // 数据层模型
import com.easynote.home.domain.model.TagModel    // UI/领域层模型

/**
 * 将数据层的 TagEntity 映射为 UI/领域层的 TagModel。
 */
fun TagEntity.toTagModel(): TagModel {
    return TagModel(
        tagId =requireNotNull(this.id) { "TagEntity.id from database cannot be null" },
        tagName = this.name ?: "",
        color =this.color?:"#FFFFFF"
    )
}
/**
 *反向映射：将 UI层的 TagModel 转换为 数据层的 TagEntity
 *主要用于新建笔记时，将选中的标签传递给 Repo 层。
 */
fun TagModel.toTagEntity(): TagEntity {
    return TagEntity(
        id = this.tagId,
        name = this.tagName,
        color = this.color
    )
}