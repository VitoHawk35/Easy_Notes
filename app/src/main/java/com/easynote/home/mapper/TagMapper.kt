package com.easynote.home.mapper // Or your data.mapper package

import com.easynote.data.entity.TagEntity // 数据层模型
import com.easynote.home.domain.model.TagModel    // UI/领域层模型

/**
 * 将数据层的 TagEntity 映射为 UI/领域层的 TagModel。
 */
fun TagEntity.toTagModel(): TagModel {
    return TagModel(
        tagId = this.tagId,
        tagName = this.tagName,
        color = this.color
    )
}
