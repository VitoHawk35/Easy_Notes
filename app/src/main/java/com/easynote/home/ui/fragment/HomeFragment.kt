package com.easynote.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.TagRepository
import com.example.mydemo.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener
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
    private val viewModel: HomeViewModel by activityViewModels {
        // 这个 Factory 逻辑和之前在 Activity 中的完全一样。
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // 使用 requireActivity().application 来安全地获取 Application 上下文
                val application = requireActivity().application
                val noteRepository = NoteRepository(application)
                val tagRepository = TagRepository(application)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application, noteRepository, tagRepository) as T
            }
        }
    }

    // 3. 将 Adapter 声明为 Fragment 的属性
    private val notePreviewAdapter = NotePreviewPagingAdapter(
        // 实现单击事件的回调
        onItemClick = { note ->
            // 在这里根据 ViewModel 的当前状态，决定单击做什么
            when (viewModel.uiMode.value) {
                is HomeUiMode.Browsing -> {
                    // TODO: 在浏览模式下，点击是进入详情页
                    // 示例：val action = HomeFragmentDirections.actionHomeToDetail(note.noteId)
                    //       findNavController().navigate(action)
                }
                is HomeUiMode.Managing -> {
                    // 在管理模式下，点击是切换选中状态
                    viewModel.toggleNoteSelection(note.noteId)
                }
            }
        },
        // 实现长按事件的回调
        onItemLongClick = { note ->
            // 长按只在浏览模式下触发进入管理模式
            if (viewModel.uiMode.value is HomeUiMode.Browsing) {
                viewModel.enterManagementMode(note.noteId)
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
        observeViewModel()
    }

    /**
     * 设置标签筛选栏的逻辑。
     */
    private fun setupTagFilterRail() {
        // 7. 【核心修改】使用 viewLifecycleOwner.lifecycleScope，它的生命周期与 Fragment 的视图绑定。
        tagFilterRail = TagFilterRail(binding.recyclerViewTagFilter, viewModel, viewLifecycleOwner.lifecycleScope)
    }

    /**
     * 设置笔记列表 RecyclerView 的逻辑。
     */
    private fun setupNotesRecyclerView() {
        binding.recyclerViewNotePreviews.adapter = notePreviewAdapter

        // 1. 直接从 ViewModel 获取当前的布局模式值
        val initialMode = viewModel.layoutMode.value
        val initialSpanCount = when (initialMode) {
            LayoutMode.LIST -> 1
            LayoutMode.GRID -> 2
        }
        binding.recyclerViewNotePreviews.layoutManager =
            StaggeredGridLayoutManager(initialSpanCount, StaggeredGridLayoutManager.VERTICAL)
    }

    /**
     * 观察来自 ViewModel 的笔记分页数据。
     */
    private fun observeViewModel() {
        // 观察笔记分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notePreviews.collectLatest { pagingData ->
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
    }

    fun onSelectAllActionClicked() {        val allNoteIds = notePreviewAdapter.snapshot().items.map { it.noteId }
        viewModel.toggleSelectAll(allNoteIds)
    }

    fun onPinActionClicked() {
        //！！！！！！！！！！！！！！！！！！后期需修改，现在是置顶选中的所有笔记
        val currentNotes = notePreviewAdapter.snapshot().items
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
     * 当 Fragment 的视图被销毁时调用。
     * 在这里清理 _binding 以防止内存泄漏。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
