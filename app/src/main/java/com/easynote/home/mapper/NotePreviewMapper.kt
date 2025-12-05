package com.easynote.home.mapper

// 从relation中获取NoteWithTags
import com.easynote.data.entity.NoteEntity
import com.easynote.data.relation.NoteWithTags
import com.easynote.home.domain.model.NotePreviewModel

/**
 * 这是一个顶级的扩展函数，专门负责将数据层的 NoteWithTags 模型
 * 映射为首页UI层需要的 NotePreviewModel。
 *
 * 它为 NoteWithTags 类“添加”了一个名为 toNotePreviewModel 的新方法。
 */
fun NoteWithTags.toNotePreviewModel(): NotePreviewModel {
    // 处理 noteEntity 本身可能为 null 的情况，提供一个默认的空 NoteEntity。
    val note = this.noteEntity ?: com.easynote.data.entity.NoteEntity()

    // 处理 tags 列表可能为 null 的情况。
    val tagList = this.tags ?: emptySet()

    return NotePreviewModel(
        noteId = requireNotNull(note.id) { "NoteEntity.id from database cannot be null" },//笔记id，应该不为空

        // 对于 title，默认为空。
        title = note.title ?: "",

        //   摘要默认为空。
        summary = note.summary ?: "",

        // 4. 从 List<TagEntity> 中安全地提取出 Set<TagModel>
        tagIds = tagList.map { it.toTagModel() }.toSet(),

        // 5. 【重要】createdTime 映射自 createTime，并处理 null 的情况。
        createdTime = note.createTime ?: 0L,

        // 6. 【重要】updatedTime 映射自 updateTime。
        updatedTime = note.updateTime ?: 0L,

        // 7. 【重要】pinnedTime 映射自 favoriteTime。
        pinnedTime = note.favoriteTime ?: 0L,

        // 8. 【重要】isPinned 映射自 isFavorite。
        isPinned = note.isFavorite ?: false
    )
}
/**
 * 🟢 [新增] 反向映射：将 NotePreviewModel 转换为 NoteWithTags
 *
 * ⚠️ 警告：NotePreviewModel 不包含 content (正文)。
 * 此方法生成的 NoteEntity 中 content 为空字符串。
 * 仅适用于【新建笔记】场景，切勿用于更新已有笔记！
 */
fun NotePreviewModel.toNoteWithTags(): NoteWithTags {

    val tagEntities = this.tagIds?.map { it.toTagEntity() }
    val defaultNote = NoteEntity(
        id = null, // ID 传 null，让 Room 自动生成主键
        title = "未命名笔记", // 🟢 指定默认标题
        summary = null, // 摘要为空
        createTime = null,
        updateTime = null,
        favoriteTime =null,
        isFavorite = false,
    )

    // 3. 返回完整的对象
    return NoteWithTags(
        noteEntity = defaultNote,
        tags = tagEntities
    )

}
