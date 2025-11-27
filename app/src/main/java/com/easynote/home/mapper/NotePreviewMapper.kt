package com.easynote.home.mapper

// 从relation中获取NoteWithTags
import com.easynote.data.relation.NoteWithTags
import com.easynote.home.domain.model.NotePreviewModel

/**
 * 这是一个顶级的扩展函数，专门负责将数据层的 NoteWithTags 模型
 * 映射为首页UI层需要的 NotePreviewModel。
 *
 * 它为 NoteWithTags 类“添加”了一个名为 toNotePreviewModel 的新方法。
 */
fun NoteWithTags.toNotePreviewModel(): NotePreviewModel {
    return NotePreviewModel(
        noteId = this.note.noteId,
        title = this.note.title,
        // 在这里执行转换逻辑，例如：从正文中截取100个字符作为摘要
        summary = this.note.content.take(20),
        // 从 Set<TagEntity> 中提取出 Set<Long>
        tagIds = this.tags.map { it.tagId }.toSet(),
        createdTime = this.note.createdTime,
        updatedTime = this.note.updatedTime,
        pinnedTime = this.note.pinnedTime,
        isPinned = this.note.isPinned
    )
}
