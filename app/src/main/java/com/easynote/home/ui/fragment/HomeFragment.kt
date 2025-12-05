package com.easynote.home.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easynote.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
import com.easynote.data.repository.impl.NoteRepositoryImpl
import com.easynote.data.repository.impl.TagRepositoryImpl
import com.easynote.detail.NoteDetailActivity
import com.easynote.home.ui.Adapter.NotePreviewPagingAdapter
import com.easynote.home.ui.Adapter.TagFilterRail

/**
 * 显示笔记列表和筛选栏的主页 Fragment。
 * 这个 Fragment 承载了之前 HomeActivity 的所有 UI 逻辑。
 */
class HomeFragment : Fragment() {

    // 1. 使用 Fragment 的 View Binding 标准模式，以安全地访问视图并防止内存泄漏。
    private var _binding: FragmentHomeBinding? = null
    // 这个属性只在 onCreateView 和 onDestroyView 之间有效。
    private val binding get() = _binding!!

    // 2. 【核心修改】使用 activityViewModels 来获取与宿主 Activity 共享的 ViewModel 实例。
    //    这确保了在多个 Fragment 之间切换时，ViewModel 的状态得以保留。
    private val viewModel: HomeViewModel by activityViewModels()

    // 3. 将 Adapter 声明为 Fragment 的属性
    private val notePreviewAdapter = NotePreviewPagingAdapter(
        // 实现单击事件的回调
        onItemClick = { note ->
            // 在这里根据 ViewModel 的当前状态，决定单击做什么
            when (viewModel.uiMode.value) {
                is HomeUiMode.Browsing -> {
                    // TODO: 在浏览模式下，点击是进入详情页
                    navigateToDetailScreen(note.noteId, note.title)
                    Log.d("HomeFragment", "跳转进noteId为: ${note.noteId}的名为：“${note.title}”笔记")
                    // 示例：val action = HomeFragmentDirections.actionHomeToDetail(note.noteId)
                    //       findNavController().navigate(action)
                }

                is HomeUiMode.Managing -> {
                    // 在管理模式下，点击是切换选中状态
                    viewModel.toggleNoteSelection(note.noteId,note.isPinned)
                }
            }
        },
        // 实现长按事件的回调
        onItemLongClick = { note ->
            // 长按只在浏览模式下触发进入管理模式
            if (viewModel.uiMode.value is HomeUiMode.Browsing) {
                viewModel.enterManagementMode(note.noteId,note.isPinned)
                true // 返回 true 表示事件已被我们消费
            } else {
                false // 在管理模式下，长按不执行任何操作
            }
        }
    )

    // 4. 将 TagFilterRail 声明为 lateinit 属性
    private lateinit var tagFilterRail: TagFilterRail

    /**
     * Fragment 创建其视图的地方。
     * 在这里加载（inflate）布局文件，并返回根视图。
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 5. 初始化 _binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 在视图创建完成后调用。
     * 这是执行所有与视图相关的初始化逻辑（如设置 Adapter、监听器）的最佳位置。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTagFilterRail()//标签筛选栏
        setupNotesRecyclerView()//笔记预览
        setupSearchListener() // 搜索框
        setupFab()//添加笔记
        observeViewModel()
        observeUiEvents()//观察ui事件

    }
    override fun onResume() {
        super.onResume()
        // 这将确保 UI 与最新的数据库状态完全同步，从而修复“新旧共存”的 bug。
        notePreviewAdapter.refresh()
        (binding.recyclerViewNotePreviews.layoutManager as? StaggeredGridLayoutManager)
            ?.invalidateSpanAssignments()
    }

    /**
     * 【新增】一个专门负责跳转到笔记详情页的方法。
     * @param noteId 要打开的笔记的 ID。如果是新建笔记，可以传入 -1L。
     * @param noteTitle 要打开的笔记的标题。
     */
    private fun navigateToDetailScreen(noteId: Long, noteTitle: String) {
        // 1. 创建一个明确指向 NoteDetailActivity 的 Intent
        val intent = Intent(requireContext(), NoteDetailActivity::class.java)

        // 2. 使用 putExtra 将数据放入 Intent 的 Bundle 中
        intent.putExtra("NOTE_ID", noteId)
        intent.putExtra("NOTE_TITLE", noteTitle)

        // 3. 启动 Activity
        startActivity(intent)
    }
    /**
     * 设置标签筛选栏的逻辑。
     */
    private fun setupTagFilterRail() {
        // 7. 【核心修改】使用 viewLifecycleOwner.lifecycleScope，它的生命周期与 Fragment 的视图绑定。
        tagFilterRail = TagFilterRail(
            binding.recyclerViewTagFilter,
            viewModel,
            viewLifecycleOwner.lifecycleScope
        )
    }

    /**
     * 设置笔记列表 RecyclerView 的逻辑。
     */
    private fun setupNotesRecyclerView() {
        binding.recyclerViewNotePreviews.adapter = notePreviewAdapter
        // 1. 保持禁用动画 (防止闪烁)
        binding.recyclerViewNotePreviews.itemAnimator = null

        val initialMode = viewModel.layoutMode.value
        val initialSpanCount = when (initialMode) {
            LayoutMode.LIST -> 1
            LayoutMode.GRID -> 2
        }

        // 2. 创建布局管理器
        val staggeredManager = StaggeredGridLayoutManager(initialSpanCount, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerViewNotePreviews.layoutManager = staggeredManager

        // 🟢 [新增] 监听数据加载状态
        // 这是解决“布局错乱/重影/留白”的终极方案
        notePreviewAdapter.addLoadStateListener { loadState ->
            // 当刷新(Refresh)结束，且不再加载(NotLoading)时
            if (loadState.refresh is androidx.paging.LoadState.NotLoading) {
                // 强制瀑布流重新计算 Item 位置
                // 这会消除因复用导致的错位，同时因为恢复了默认 Gap 策略，也不会留白
                staggeredManager.invalidateSpanAssignments()
            }
        }
    }

    /**
     * 观察来自 ViewModel 的笔记分页数据。
     */
    private fun observeViewModel() {
        // 观察笔记分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.homeNotePreviews.collectLatest { pagingData ->
                notePreviewAdapter.submitData(pagingData)
            }
        }

        // 4. 【新增】观察UI模式的变化，并据此更新UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiMode.collect { mode ->
                // a. 更新 Adapter 的内部UI（例如，显示/隐藏复选框）
                notePreviewAdapter.currentUiMode = mode

                // b. 获取宿主 Activity 的引用
                val mainActivity = requireActivity() as? HomeActivity // 假设你的主Activity叫MainActivity

                // c. 调用宿主 Activity 的公开方法来切换底部导航栏
                mainActivity?.showManagementUI(mode is HomeUiMode.Managing)

                // d. 让顶部的菜单栏失效，以便根据新状态重新绘制（比如显示“全选”按钮）
                requireActivity().invalidateOptionsMenu()

                // e.根据是否是管理模式，来显示或隐藏浮动添加按钮
                binding.fabAddNote.isVisible = mode !is HomeUiMode.Managing
            }
        }
        // 【新增】观察布局模式的变化，并更新 RecyclerView 的 spanCount
        viewLifecycleOwner.lifecycleScope.launch {        viewModel.layoutMode.collect { mode ->
            val spanCount = when (mode) {
                LayoutMode.LIST -> 1 // 列表模式为1列
                LayoutMode.GRID -> 2 // 宫格模式为2列
            }
            // 安全地获取并更新 StaggeredGridLayoutManager 的 spanCount
            (binding.recyclerViewNotePreviews.layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount
        }
        }
        // 观察排序方式
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sortOrder.collect { order ->
                // 更新 Adapter 的排序状态，触发时间显示刷新
                notePreviewAdapter.currentSortOrder = order
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filterState.collect {
                // 切换标签了，直接归零，简单粗暴且有效
                binding.recyclerViewNotePreviews.scrollToPosition(0)
            }
        }

        // 监听搜索变化同理
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect {
                binding.recyclerViewNotePreviews.scrollToPosition(0)
            }
        }

    }
    // 监听 ViewModel 发来的一次性事件
    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 注意：uiEvent 是 Channel 转换的 Flow，不需要 repeatOnLifecycle 也可以，
            // 但为了安全起见，通常放在生命周期感知的作用域里
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is HomeUiEvent.NavigateToDetail -> {
                        navigateToDetailScreen(event.noteId, "未命名笔记") // 标题为空
                    }
                }
            }
        }
    }


    fun onPinActionClicked() {
        viewModel.pinSelectedNotes()

    }

    fun onDeleteActionClicked() {
        viewModel.deleteSelectedNotes()
    }
    /**
     * 为搜索框设置文本变化监听器。
     */
    private fun setupSearchListener() {
        binding.editTextSearch.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable.toString())
        }
    }
    /**
     * 为浮动添加按钮设置点击事件监听器。
     */
    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            // 点击添加按钮，跳转到笔记详情页，但不传递任何笔记ID和标题（表示是新建笔记）,但是传递标签，如果有选中的话
            viewModel.createNewNote(withCurrentTags = true)
        }
    }

    /**
     * 当 Fragment 的视图被销毁时调用。
     * 在这里清理 _binding 以防止内存泄漏。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
