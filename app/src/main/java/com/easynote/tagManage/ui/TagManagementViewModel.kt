package com.easynote.home.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope // 必须导入这个扩展属性
import com.easynote.data.entity.TagEntity
import com.easynote.data.repository.TagRepository
import com.easynote.home.domain.model.TagModel
import com.easynote.home.mapper.toTagModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch // 必须导入 launch

// UI 事件，用于通知 Activity 弹 Toast
sealed class TagUiEvent {
    data class ShowToast(val message: String) : TagUiEvent()
}

class TagManagementViewModel(
    application: Application,
    private val tagRepository: TagRepository
) : AndroidViewModel(application) {

    // 1. 获取所有标签列表 (Entity -> Model)
    // 这里假设你的 Entity 字段是 tagId, tagName, color
    val tags: Flow<List<TagModel>> = tagRepository.getAllTagsFlow().map { entities ->
        entities.map {
            entity -> entity.toTagModel()
        }
    }

    // 2. UI 事件通道
    private val _uiEvent = Channel<TagUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 3. 插入标签
    fun insertTag(name: String, color: String) {
        viewModelScope.launch {
            tagRepository.insertTag(TagEntity(name = name, color = color))
        }
    }

    // 4. 更新标签
    fun updateTag(id: Long, name: String, color: String) {
        viewModelScope.launch {
            tagRepository.updateTag(TagEntity(id = id, name = name, color = color))
        }
    }

    /**
     * 5. 执行删除逻辑
     * 调用 Repo 的 deleteTagById。
     * 根据你的约定：返回值为该标签下的笔记数量。
     * - 如果 > 0: 说明有关联笔记，不可删除，弹 Toast。
     * - 如果 == 0: 说明已成功删除 (Repo 层已执行 delete)。
     */
    fun deleteTag(tag: TagModel) {
        viewModelScope.launch {
            // 调用 Repo，Repo 内部逻辑应为：先 count，如果 count == 0 则 delete，最后 return count
            val noteCount = tagRepository.deleteTagById(tag.tagId)
            //  TODO等数据层修改删除返回数据，根据返回的标签下笔记大小弹吐司。
/*            if (noteCount > 0) {
                // 删除失败，提示用户
                _uiEvent.send(TagUiEvent.ShowToast("“${tag.tagName}”下有 $noteCount 条笔记，不可删除"))
            } else {
                // 删除成功，无需额外操作，Flow 会自动更新 UI
                _uiEvent.send(TagUiEvent.ShowToast("标签已删除"))
            }*/
        }
    }
}