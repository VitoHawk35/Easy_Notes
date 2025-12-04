package com.easynote.home.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.easynote.databinding.FragmentCalendarBinding
import com.easynote.detail.NoteDetailActivity
import com.easynote.home.ui.Adapter.CalendarAdapter
import com.easynote.home.ui.Adapter.NotePreviewListAdapter
import com.easynote.home.ui.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.easynote.util.DateUtils
import android.util.Log
import com.easynote.home.ui.HomeUiEvent

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // 使用 activityViewModels 复用 Activity 级别的 ViewModel
    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // 日历格子 Adapter
    private val calendarAdapter = CalendarAdapter { day ->
        viewModel.onCalendarDateSelected(day)
    }

    // 【核心修改】底部列表 Adapter
    // 使用 NotePreviewListAdapter (继承自 ListAdapter)，而非 PagingDataAdapter
    private val noteListAdapter = NotePreviewListAdapter(
        onItemClick = { note ->
            // 跳转到详情页
            navigateToDetailScreen(note.noteId,note.title)
        },
        onItemLongClick = {
         /*   // 长按只在浏览模式下触发进入管理模式
                note ->
            if (viewModel.uiMode.value is HomeUiMode.Browsing) {
                viewModel.enterManagementMode(note.noteId,note.isPinned)
                true // 返回 true 表示事件已被我们消费
            } else {
                false // 在管理模式下，长按不执行任何操作
            }*/
            false// 日历模式下暂不支持长按管理，返回 false
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 监听年月选择器的回调
        childFragmentManager.setFragmentResultListener(
            YearMonthPickerDialogFragment.REQUEST_KEY,
            this
        ) { _, bundle ->
            val year = bundle.getInt(YearMonthPickerDialogFragment.RESULT_KEY_YEAR)
            val month = bundle.getInt(YearMonthPickerDialogFragment.RESULT_KEY_MONTH)
            val currentState = viewModel.calendarState.value

            // 只有年月真正变化时才通知 ViewModel
            if (year != 0 && month != 0 && (year != currentState.selectedYear || month != currentState.selectedMonth)) {
                viewModel.onCalendarMonthChanged(year, month)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        observeUiEvents()
    }


    private fun setupUI() {
        // 1. 配置日历 RecyclerView
        binding.recyclerViewCalendar.apply {
            adapter = calendarAdapter
            layoutManager = GridLayoutManager(requireContext(), 7)
            isNestedScrollingEnabled = false // 禁止日历内部滚动，依赖外部 ScrollView 或 BottomSheet
        }

        // 2. 配置底部笔记列表 RecyclerView
        binding.recyclerViewNotePreviews.apply {
            adapter = noteListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 3. 配置 BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        // 监听日历容器高度变化，动态调整 BottomSheet 的位置 (PeekHeight)
        // 这样可以处理 5 行月和 6 行月的高度差异
        binding.calendarContainer.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if ((bottom - top) != (oldBottom - oldTop)) {
                updatePeekHeight()
            }
        }
        // 初始计算一次高度
        updatePeekHeight()

        // 4. 点击年月标题和图标，弹出选择器
        binding.headerYearMonthContainer.setOnClickListener {
            val currentState = viewModel.calendarState.value
            YearMonthPickerDialogFragment.newInstance(currentState.selectedYear, currentState.selectedMonth)
                .show(childFragmentManager, "YearMonthPicker")
        }

        // 5. 拖动把手点击事件 (切换折叠/展开)
        binding.dragHandleArea.setOnClickListener {
            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                BottomSheetBehavior.STATE_EXPANDED
            } else {
                BottomSheetBehavior.STATE_COLLAPSED
            }
            bottomSheetBehavior.state = state
        }

        // 6. 监听 BottomSheet 滑动，处理日历变暗动画
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 0 (collapsed) -> 1 (expanded)
                binding.calendarContainer.alpha = 1f - slideOffset
            }
        })
        // 新建笔记按键，跳转新建笔记
        binding.fabAddNoteToday.setOnClickListener {
            viewModel.createNewNote(withCurrentTags = true)
        }
    }

    /**
     * 动态计算 PeekHeight，确保 BottomSheet 刚好停在日历下方
     */
    private fun updatePeekHeight() {
        binding.root.post {
            if (_binding == null) return@post

            val parentHeight = binding.coordinatorLayout.height
            val calendarHeight = binding.calendarContainer.height
            val minPeekHeight = (60 * resources.displayMetrics.density).toInt() // 至少保留把手高度

            // 剩余空间 = 屏幕高度 - 日历高度
            val targetPeekHeight = parentHeight - calendarHeight

            // 防止日历太长导致 PeekHeight 过小或为负
            val finalPeekHeight = if (targetPeekHeight > minPeekHeight) {
                targetPeekHeight
            } else {
                minPeekHeight
            }

            if (bottomSheetBehavior.peekHeight != finalPeekHeight) {
                bottomSheetBehavior.peekHeight = finalPeekHeight
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. 监听日历基础状态 (年月变化) -> 更新标题和 Adapter 结构
                launch {
                    viewModel.calendarState.collect { state ->
                        binding.textViewYearMonth.text = "${state.selectedYear}年${state.selectedMonth}月"
                        calendarAdapter.updateData(state.selectedYear, state.selectedMonth, state.selectedDay)
                        // 数据结构变化可能导致行数变化，再次校准高度
                        binding.root.post { updatePeekHeight() }
                        // 判断选中日期是否为“今天”，控制新建笔记按键的显示
                        if(state.selectedDay==null||!DateUtils.isToday(state.selectedYear, state.selectedMonth, state.selectedDay)){
                            binding.fabAddNoteToday.visibility = View.GONE
                        }else{
                            binding.fabAddNoteToday.visibility = View.VISIBLE
                        }
                    }
                }

                // 2. 监听日历格子内容 (Map 数据) -> 更新日历上的标题和小红点
                launch {
                    viewModel.calendarDailyData.collect { map ->
                        calendarAdapter.updateDailyData(map)
                    }
                }

                // 3. 监听全月笔记 + 选中日期 -> 更新底部列表
                // 使用 combine 结合两个流，当任意一个变化时重新计算列表内容
                launch {
                    combine(
                        viewModel.calendarNotePreviews, // 本月所有笔记 (List)
                        viewModel.calendarState       // 当前选中的日期
                    ) { allNotes, state ->
                        val selectedDay = state.selectedDay

                        if (selectedDay == null) {
                            // 如果没选中具体哪天，显示本月所有笔记
                            allNotes
                        } else {
                            // 如果选中了某天，只筛选出那一天的笔记 (使用 createdTime 判断)
                            val (start, end) = DateUtils.getDayRange(state.selectedYear, state.selectedMonth, selectedDay)
                            allNotes.filter { note ->
                                note.createdTime in start..end
                            }
                        }
                    }.collect { filteredList ->
                        // 将筛选后的 List 提交给 ListAdapter
                        // 因为是 ListAdapter，它会自动计算 Diff 并刷新动画
                        noteListAdapter.submitList(filteredList) {
                            // 只要列表内容发生了切换（比如选了日期，或者切回了整月），就回到顶部
                            binding.recyclerViewNotePreviews.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }
    // 🟢 [新增] 监听 ViewModel 发来的一次性事件
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
    private fun navigateToDetailScreen(noteId: Long, noteTitle: String?) {
        val intent = Intent(requireContext(), NoteDetailActivity::class.java)
        intent.putExtra("NOTE_ID", noteId)
        intent.putExtra("NOTE_TITLE", noteTitle)
        startActivity(intent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}