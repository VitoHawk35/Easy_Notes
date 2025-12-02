package com.easynote.home.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.easynote.data.annotation.NoteOrderWay
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
import kotlin.Int
import kotlin.Long

//筛选接口
sealed interface FilterState//抽象筛选状态接口，表明某个属性是不是属于筛选标签的一部分
data object FilterAll : FilterState//“全部”筛选标签
data class FilterByTags(val selectedTagIds: Set<Long>) : FilterState //选中的标签
// UI模式，管理或者预览
sealed interface HomeUiMode {
    object Browsing : HomeUiMode // 浏览模式
    /**
     * 管理模式状态，现在包含两个独立的Set来跟踪置顶和非置顶的选中项。
     */
    data class Managing(
        val selectedPinnedIds: Set<Long> = emptySet(),//选中的已置顶笔记集合
        val selectedUnpinnedIds: Set<Long> = emptySet()//选中的未置顶笔记集合
    ) : HomeUiMode {
        // 用于获取所有选中的ID
        val allSelectedIds: Set<Long> get() = selectedPinnedIds + selectedUnpinnedIds
        // 判断当前是否有选中项
        val isSelectionEmpty: Boolean get() = selectedPinnedIds.isEmpty() && selectedUnpinnedIds.isEmpty()
    }
}
// 设置项的枚举类
enum class SortOrder(val dataLayerValue: String) {


    //TODO要修改对应的设置“”对应的string
    BY_UPDATE_TIME("UPDATE_TIME_DESC"),
    BY_CREATION_TIME("UPDATE_TIME_ASC")
}
// 查询参数类
data class NoteQuery(
    val filterState: FilterState,//筛选栏
    val sortOrder: SortOrder,//排序方式
    val searchQuery: String= "", // 默认搜索为空
    val startDate: Long? = null, // 开始时间戳，默认为空
    val endDate: Long? = null   // 结束时间戳，默认为空
)
//笔记预览的排列模式
enum class LayoutMode {
    LIST, // 列表模式 (spanCount = 1)
    GRID  // 宫格模式 (spanCount = 2)
}
//管理模式下底部菜单栏置顶/取消置顶按钮应该处于的状态

enum class PinActionState {
    PIN,    // 应该显示“置顶”图标，执行置顶操作
    UNPIN   // 应该显示“取消置顶”图标，执行取消置顶操作
}
class HomeViewModel(
    application: Application,
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository
): AndroidViewModel(application){
    //通过stateflow管理当前ui模式
    private val _uiMode = MutableStateFlow<HomeUiMode>(HomeUiMode.Browsing)
    val uiMode: StateFlow<HomeUiMode> = _uiMode
    //管理置顶//取消置顶按键，当选中的标签的置顶非置顶集合发生变化时，底部按键也自动变化。
    val pinActionState: StateFlow<PinActionState> = combine(uiMode) { (mode) ->
        if (mode is HomeUiMode.Managing && mode.selectedUnpinnedIds.isNotEmpty()) {
            PinActionState.PIN
        } else {
            PinActionState.UNPIN
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PinActionState.PIN
    )
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
    private val _dateRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)    // 监听笔记日期范围状态
    val dateRange: StateFlow<Pair<Long?, Long?>> = _dateRange;
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
        combine(_filterState, _sortOrder, _searchQuery,_dateRange) {
            filter, sort, query,dateRange ->
            NoteQuery(filter, sort, query,dateRange.first,dateRange.second) // 将三个状态合并成一个查询对象
        }.debounce(300L) //使用 debounce 来防止用户输入过快导致频繁查询数据库只有当用户停止输入300毫秒后，才执行后面的操作
        .flatMapLatest { query ->
            // 在调用 repository 时，同时传入筛选条件和排序条件
            Log.d("HomeViewModel", "触发一次flow收集数据")
            val sortOrderString = query.sortOrder.dataLayerValue
            when (val filterState = query.filterState) {
                is FilterAll -> noteRepository.getAllNotePagingFlow(
                    10,
                    null,
                    query.searchQuery.ifEmpty { null },
                    query.startDate,
                    query.endDate,
                    sortOrderString
                )
                is FilterByTags -> noteRepository.getAllNotePagingFlow(
                    10,
                    filterState.selectedTagIds,
                    query.searchQuery.ifEmpty { null },
                    query.startDate,
                    query.endDate,
                    sortOrderString
                )

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
    // 处理侧边栏日期筛选
    fun applyDateFilter(start: Long?, end: Long?) {
        _dateRange.value = start to end
    }
    //清除日期筛选
    fun clearDateFilter() {
        _dateRange.value = null to null
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
     * @param isPinned 第一个被选中的笔记的置顶状态。
     */
    fun enterManagementMode(noteId: Long, isPinned: Boolean) {
        val initialState = if (isPinned) {
            HomeUiMode.Managing(selectedPinnedIds = setOf(noteId))
        } else {
            HomeUiMode.Managing(selectedUnpinnedIds = setOf(noteId))
        }
        _uiMode.value = initialState
    }
    /**
     * 退出管理模式
     */
    fun exitManagementMode() {
        _uiMode.value = HomeUiMode.Browsing
    }

    /**
     * 在管理模式下，当用户点击一个笔记项时调用。
     * 这个方法现在只在内存中操作 Set，无需访问数据库。
     * @param noteId 被点击的笔记的ID。
     * @param isPinned 被点击的笔记的置顶状态。
     */
    fun toggleNoteSelection(noteId: Long, isPinned: Boolean) {
        val currentMode = _uiMode.value
        if (currentMode is HomeUiMode.Managing) {
            val newPinnedIds = currentMode.selectedPinnedIds.toMutableSet()
            val newUnpinnedIds = currentMode.selectedUnpinnedIds.toMutableSet()

            // 判断应该在哪个Set中操作
            val wasPinned = noteId in newPinnedIds
            val wasUnpinned = noteId in newUnpinnedIds

            if (wasPinned) {
                newPinnedIds.remove(noteId)
            } else if (wasUnpinned) {
                newUnpinnedIds.remove(noteId)
            } else {
                // 如果之前未被选中，则根据其isPinned状态添加到对应的Set
                if (isPinned) {
                    newPinnedIds.add(noteId)
                } else {
                    newUnpinnedIds.add(noteId)
                }
            }

            val newMode = HomeUiMode.Managing(newPinnedIds, newUnpinnedIds)
            // 如果所有笔记都取消选中了，自动退出管理模式//
            // TODO:所有笔记选中，底部功能栏置灰不让点击。而不是退出
            if (newMode.isSelectionEmpty) {
                exitManagementMode()
            } else {
                _uiMode.value = newMode
            }
        }
    }

    /**
     * 执行批量删除操作。
     */
    fun deleteSelectedNotes() {
        val currentMode = _uiMode.value
        if (currentMode is HomeUiMode.Managing && !currentMode.isSelectionEmpty) {
            val idsToDelete = currentMode.allSelectedIds
            viewModelScope.launch {
                // TODO:调用删除接口，传入要删除笔记的id
                // noteRepository.deleteNotesByIds(idsToDelete)
                exitManagementMode()//退出管理模式
            }
        }
    }
    /**
     * 根据当前的 PinActionState 执行置顶或取消置顶操作。
     */
    fun pinSelectedNotes() {
        val currentMode = _uiMode.value
        val action = pinActionState.value // 获取当前应该执行的操作

        if (currentMode is HomeUiMode.Managing && !currentMode.isSelectionEmpty) {
            viewModelScope.launch {
                when (action) {
                    PinActionState.PIN -> {
                        // 如果是置顶操作，只处理那些未置顶的选中项
                        val idsToPin = currentMode.selectedUnpinnedIds
                        if (idsToPin.isNotEmpty()) {
                            Log.d("HomeViewModel", "Pinning notes: $idsToPin")
                            //TODO:调用置顶笔记接口
                            // TODO: noteRepository.pinNotesByIds(idsToPin)
                        }
                    }
                    PinActionState.UNPIN -> {
                        // 如果是取消置顶操作，只处理那些已置顶的选中项
                        val idsToUnpin = currentMode.selectedPinnedIds
                        if (idsToUnpin.isNotEmpty()) {
                            Log.d("HomeViewModel", "Unpinning notes: $idsToUnpin")
                            //TODO:调用取消置顶笔记接口
                            // TODO: noteRepository.unpinNotesByIds(idsToUnpin)
                        }
                    }
                }
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
            val allSelected = currentMode.allSelectedIds.containsAll(allNoteIds)
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
