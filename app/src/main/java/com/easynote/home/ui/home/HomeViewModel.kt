package com.easynote.home.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.domain.model.TagModel
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.TagRepository
import com.easynote.home.mapper.toNotePreviewModel
import com.easynote.home.mapper.toTagModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

// 定义筛选状态
sealed interface FilterState//抽象筛选状态接口，表明某个属性是不是属于筛选标签的一部分
//“全部”筛选标签
data object FilterAll : FilterState
//选中的标签
data class FilterByTags(val selectedTagIds: Set<Long>) : FilterState

class HomeViewModel(
    application: Application,
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository
): AndroidViewModel(application){

    // 1. 筛选状态：管理当前的筛选模式。默认是选中“全部”。
    private val _filterState = MutableStateFlow<FilterState>(FilterAll)
    val filterState: StateFlow<FilterState> = _filterState

    // 2. 标签数据：用于首页预览有哪些标签可供筛选，分页加载所有标签，保持不变。UI可以用它来获取完整的TagModel对象。
    val tags: Flow<PagingData<TagModel>> = tagRepository.getPagingTags().map{
        pagingData: PagingData<TagEntity> -> // 2. 使用 Flow 的 .map 操作符
        // 3. 对 PagingData 内部的每一项 TagEntity 进行转换
        pagingData.map { tagEntity ->
            tagEntity.toTagModel() // 4. 调用映射函数
        }
    }.cachedIn(viewModelScope) // 5. 缓存最终转换好的 Flow

    // 3. 笔记预览数据，根据筛选状态（_filterState）的变化，动态切换笔记数据源。
    // 通过flatMapLates来使得用户多次点击标签后，快速得到最后一次筛选状态的数据
        @OptIn(ExperimentalCoroutinesApi::class)
        val notePreviews: Flow<PagingData<NotePreviewModel>> = _filterState
        //监听上流筛选状态根据状态选择对应的repo方法获取对应的笔记数据流
        .flatMapLatest<FilterState, Flow<PagingData<NoteWithTags>>> { state ->
            when (state) {
                is FilterAll -> noteRepository.getPagingNotePreviews()
                is FilterByTags -> noteRepository.getPagingNotePreviews(state.selectedTagIds)
            }
        }
        // 第二步：对上一步流出的 Flow<PagingData<NoteWithTags>> 进行类型映射。
        // 这个 .map 是作用在 Flow 上的标准操作符。
        .map { pagingData: PagingData<NoteWithTags> ->
            // 在 .map 内部，我们对 PagingData 对象进行转换。
            // 这里调用的是 Paging 3 为 PagingData 提供的 map 函数。
            pagingData.map { noteWithTags ->
                // 调用你定义在 mapper 文件中的扩展函数进行转换。
                noteWithTags.toNotePreviewModel()
            }
        }
        // 第三步：对最终转换好的、类型正确的 Flow<PagingData<NotePreviewModel>> 进行缓存。
        .cachedIn(viewModelScope)

    // --- 事件处理方法 (Event Handlers)，由UI调用 ---

    /**
     * 当用户点击“全部”标签时调用。
     */
    fun onSelectAll() {
        if (_filterState.value !is FilterAll) {
            _filterState.value = FilterAll
        }
    }

    /**
     * 当用户点击一个自定义标签时调用。
     * 【优化】函数签名不变，仍然接收 TagModel，但内部逻辑已更新为只处理ID。
     * @param tag 被点击的标签对象，由UI层直接提供。
     */
    fun onTagSelected(tag: TagModel) {
        val tagId = tag.tagId // 在ViewModel内部提取ID
        val currentSelectedIds = when (val currentState = _filterState.value) {
            // 如果之前是“全部”状态，则新的ID集合只包含当前点击的ID
            is FilterAll -> mutableSetOf(tagId)
            // 如果之前已经是多选状态，则在现有ID集合上操作
            is FilterByTags -> currentState.selectedTagIds.toMutableSet().apply {
                if (tagId in this) {
                    remove(tagId) // 如果ID已存在，则移除
                } else {
                    add(tagId) // 如果ID不存在，则添加
                }
            }
        }

        // 如果操作后，所有标签都没选中，则自动切换回“全部”的筛选标签
        if (currentSelectedIds.isEmpty()) {
            _filterState.value = FilterAll
        } else {
            // 否则，用新的筛选标签集合更新笔记预览
            _filterState.value = FilterByTags(selectedTagIds = currentSelectedIds)
        }
    }
}
