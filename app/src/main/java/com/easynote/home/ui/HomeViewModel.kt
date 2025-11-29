package com.easynote.home.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.domain.model.TagModel
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.TagRepository
import com.easynote.data.entity.TagEntity
import com.easynote.data.relation.NoteWithTags
import com.easynote.home.mapper.toNotePreviewModel
import com.easynote.home.mapper.toTagModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

//筛选管理
sealed interface FilterState//抽象筛选状态接口，表明某个属性是不是属于筛选标签的一部分
data object FilterAll : FilterState//“全部”筛选标签
data class FilterByTags(val selectedTagIds: Set<Long>) : FilterState //选中的标签
// UI模式，管理或者预览
sealed interface HomeUiMode {
    object Browsing : HomeUiMode // 浏览模式
    data class Managing(val selectedNoteIds: Set<Long>) : HomeUiMode // 管理模式，并持有已选中笔记的ID
}
// 设置项的枚举类
enum class SortOrder {
    BY_UPDATE_TIME,
    BY_CREATION_TIME
}
// 查询参数类
data class NoteQuery(
    val filterState: FilterState,//筛选栏
    val sortOrder: SortOrder,//排序方式
    val searchQuery: String = "" // 默认搜索为空
)
enum class LayoutMode {
    LIST, // 列表模式 (spanCount = 1)
    GRID  // 宫格模式 (spanCount = 2)
}
class HomeViewModel(
    application: Application,
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository
): AndroidViewModel(application){
    //通过stateflow管理当前ui模式
    private val _uiMode = MutableStateFlow<HomeUiMode>(HomeUiMode.Browsing)
    val uiMode: StateFlow<HomeUiMode> = _uiMode
    // 1筛选状态：管理当前的筛选模式。默认是选中“全部”。
    private val _filterState = MutableStateFlow<FilterState>(FilterAll)
    val filterState: StateFlow<FilterState> = _filterState
    // 监听搜索 StateFlow
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    // 设置项状态
    private val _sortOrder = MutableStateFlow(SortOrder.BY_UPDATE_TIME) // 默认按更新时间
    val sortOrder: StateFlow<SortOrder> = _sortOrder
    private val _layoutMode = MutableStateFlow(LayoutMode.GRID) // 默认是宫格模式
    val layoutMode: StateFlow<LayoutMode> = _layoutMode
    // 2. 标签数据：用于首页预览有哪些标签可供筛选，分页加载所有标签，保持不变。UI可以用它来获取完整的TagModel对象。
    val tags: Flow<PagingData<TagModel>> = tagRepository.getPagingTagsFlow(5).map<PagingData<TagEntity>, PagingData<TagModel>>{
        pagingData: PagingData<TagEntity> -> // 2. 使用 Flow 的 .map 操作符
        // 3. 对 PagingData 内部的每一项 TagEntity 进行转换
        pagingData.map { tagEntity ->
            tagEntity.toTagModel() // 4. 调用映射函数
        }
    }.cachedIn(viewModelScope) // 5. 缓存最终转换好的 Flow

    // 3. 笔记预览数据，根据筛选状态（_filterState）的变化和排序方式，动态切换笔记数据源。
    // 通过flatMapLates来使得用户多次点击标签后，快速得到最后一次筛选状态的数据
    @OptIn(ExperimentalCoroutinesApi::class)
    val notePreviews: Flow<PagingData<NotePreviewModel>> =
        // 2. combine监听三个流：筛选、排序和搜索
        combine(_filterState, _sortOrder, _searchQuery) { filter, sort, query ->
            NoteQuery(filter, sort, query) // 将三个状态合并成一个查询对象
        } // 使用 debounce 来防止用户输入过快导致频繁查询数据库
             .debounce(300L) // 只有当用户停止输入300毫秒后，才执行后面的操作
        .flatMapLatest { query ->
            // 在调用 repository 时，同时传入筛选条件和排序条件
            // 注意：你需要修改 Repository 和 DAO 来接收 SortOrder 参数
            when (query.filterState) {
                /////////////////////////////!!!!!!!!!!这个后续还要根据不同筛选状态和数据库同学对接，根据query.searchQuery和query.sortOrder
                is FilterAll -> noteRepository.getAllNoteWithTagsPagingFlow(10, "ORDER_UPDATE_TIME_DESC")
                is FilterByTags -> noteRepository.getAllNoteWithTagsPagingFlow(10, "ORDER_UPDATE_TIME_DESC")

            }
        } //监听上流筛选状态和排序方式的repo方法获取对应的笔记数据流
        // 对上一步流出的 Flow<PagingData<NoteWithTags>> 进行类型映射。
        .map<PagingData<NoteWithTags>, PagingData<NotePreviewModel>> { pagingData: PagingData<NoteWithTags> ->
            // 在 .map 内部，我们对 PagingData 对象进行转换。
            // 这里调用的是 Paging 3 为 PagingData 提供的 map 函数。
            pagingData.map { noteWithTags ->
                // 调用你定义在 mapper 文件中的扩展函数进行转换。
                noteWithTags.toNotePreviewModel()
            }
        }
        // 第三步：对最终转换好的、类型正确的 Flow<PagingData<NotePreviewModel>> 进行缓存。
        .cachedIn(viewModelScope)

//////// --- 事件处理方法 (Event Handlers)，由UI调用 ---

    /**
     * 当用户点击“全部”标签时调用。
     */
    fun onSelectAll() {
        if (_filterState.value !is FilterAll) {
            _filterState.value = FilterAll
        }
    }
    /**
     * 当用户修改排序方式
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        // TODO: 在这里将设置持久化到 DataStore 或 SharedPreferences
    }
    /**
     * 当用户修改笔记预览展示方式方式
     */
    fun setLayoutMode(mode: LayoutMode) {
        _layoutMode.value = mode
        // TODO: 在这里将设置持久化
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
    // 4. 更新搜索关键词的方法，供 HomeFragment 调用
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    /**
     * 当用户长按笔记，进入管理模式时调用。
     * @param noteId 第一个被选中的笔记的ID。
     */
    fun enterManagementMode(noteId: Long) {
        _uiMode.value = HomeUiMode.Managing(selectedNoteIds = setOf(noteId))
    }
    /**
     * 当用户在管理模式下点击返回或完成时，退出管理模式。
     */
    fun exitManagementMode() {
        _uiMode.value = HomeUiMode.Browsing
    }

    /**
     * 在管理模式下，当用户点击一个笔记项时调用。
     * @param noteId 被点击的笔记的ID。
     */
    fun toggleNoteSelection(noteId: Long) {
        val currentMode = _uiMode.value
        if (currentMode is HomeUiMode.Managing) {
            val currentSelectedIds = currentMode.selectedNoteIds.toMutableSet()
            if (noteId in currentSelectedIds) {
                currentSelectedIds.remove(noteId)
            } else {
                currentSelectedIds.add(noteId)
            }
            // 如果所有笔记都取消选中了，自动退出管理模式
            if (currentSelectedIds.isEmpty()) {
                exitManagementMode()
            } else {
                _uiMode.value = HomeUiMode.Managing(currentSelectedIds)
            }
        }
    }

    /**
     * 执行批量删除操作。
     */
    fun deleteSelectedNotes() {
        val currentMode = _uiMode.value
        if (currentMode is HomeUiMode.Managing) {
            val idsToDelete = currentMode.selectedNoteIds
            viewModelScope.launch {
                // TODO: 在这里将设置批量删除
                // noteRepository.deleteNotesByIds(idsToDelete) // 假设Repository有这个方法
                exitManagementMode() // 删除后退出管理模式
            }
        }
    }
    /**
     * 置顶/取消置顶操作。！！！！！！！！！！！后期可能修改，现在默认将选中的所有笔记都置顶
     */
    fun pinSelectedNotes() {
        val currentMode = _uiMode.value
        // 确保当前处于管理模式，并且有选中的笔记
        if (currentMode is HomeUiMode.Managing && currentMode.selectedNoteIds.isNotEmpty()) {
            val idsToTogglePin = currentMode.selectedNoteIds
            // 在 viewModelScope 中启动一个协程来执行数据库操作
            viewModelScope.launch {
                // 调用 Repository 的方法来切换置顶状态
                // 假设 Repository 有一个 togglePinStatusForNotes 方法
                // TODO: 在这里将设置批量置顶
                //noteRepository.togglePinStatusForNotes(idsToTogglePin)
                // 操作完成后，退出管理模式
                exitManagementMode()
            }
        }
    }

    /**
     * 全选/取消全选
     * @param allNoteIds 当前列表中的所有笔记ID
     */
    fun toggleSelectAll(allNoteIds: List<Long>) {
        val currentMode = _uiMode.value
        if (currentMode is HomeUiMode.Managing) {
            val allSelected = currentMode.selectedNoteIds.containsAll(allNoteIds)
            if (allSelected) {
                // 如果已全选，则全部取消
                exitManagementMode()
            } else {
                // 如果未全选，则选中全部
                _uiMode.value = HomeUiMode.Managing(allNoteIds.toSet())
            }
        }
    }
}
