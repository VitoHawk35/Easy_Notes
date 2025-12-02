package com.easynote.home.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easynote.databinding.FragmentCalendarBinding
import com.easynote.home.ui.Adapter.CalendarAdapter
import com.easynote.home.ui.Adapter.NotePreviewPagingAdapter
import com.easynote.home.ui.HomeViewModel
import com.easynote.home.ui.LayoutMode
import com.easynote.home.ui.Screen
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val calendarAdapter = CalendarAdapter { day ->
        viewModel.onCalendarDateSelected(day)
    }

    private val notePreviewAdapter = NotePreviewPagingAdapter(
        onItemClick = { note -> /* TODO: 导航 */ },
        onItemLongClick = { false }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            YearMonthPickerDialogFragment.REQUEST_KEY,
            this
        ) { _, bundle ->
            val year = bundle.getInt(YearMonthPickerDialogFragment.RESULT_KEY_YEAR)
            val month = bundle.getInt(YearMonthPickerDialogFragment.RESULT_KEY_MONTH)
            val currentState = viewModel.calendarState.value
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
    }

    override fun onResume() {
        super.onResume()
        viewModel.setCurrentScreen(Screen.Calendar)
    }

    private fun setupUI() {
        // 1. 设置日历 Adapter
        binding.recyclerViewCalendar.adapter = calendarAdapter
        binding.recyclerViewCalendar.isNestedScrollingEnabled = false
        binding.recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)

        // 2. 设置笔记列表 Adapter
        binding.recyclerViewNotePreviews.adapter = notePreviewAdapter
        val initialMode = viewModel.layoutMode.value
        val initialSpanCount = if (initialMode == LayoutMode.LIST) 1 else 2
        binding.recyclerViewNotePreviews.layoutManager =
            StaggeredGridLayoutManager(initialSpanCount, StaggeredGridLayoutManager.VERTICAL)

        // 3. 初始化 BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        // 监听日历容器高度变化，自动调整 PeekHeight (处理6行日历遮挡问题)
        binding.calendarContainer.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if ((bottom - top) != (oldBottom - oldTop)) {
                updatePeekHeight()
            }
        }

        updatePeekHeight()

        // 点击事件
        binding.textViewYearMonth.setOnClickListener {
            val currentState = viewModel.calendarState.value
            YearMonthPickerDialogFragment.newInstance(currentState.selectedYear, currentState.selectedMonth)
                .show(childFragmentManager, "YearMonthPicker")
        }

        binding.dragHandleArea.setOnClickListener {
            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                BottomSheetBehavior.STATE_EXPANDED
            } else {
                BottomSheetBehavior.STATE_COLLAPSED
            }
            bottomSheetBehavior.state = state
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.calendarContainer.alpha = 1f - slideOffset
            }
        })
    }

    private fun updatePeekHeight() {
        binding.root.post {
            if (_binding == null) return@post
            val parentHeight = binding.coordinatorLayout.height
            val calendarHeight = binding.calendarContainer.height
            val minPeekHeight = (60 * resources.displayMetrics.density).toInt()
            val targetPeekHeight = parentHeight - calendarHeight

            val finalPeekHeight = if (targetPeekHeight > minPeekHeight) targetPeekHeight else minPeekHeight

            if (bottomSheetBehavior.peekHeight != finalPeekHeight) {
                bottomSheetBehavior.peekHeight = finalPeekHeight
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. 监听日历年月变化
                launch {
                    viewModel.calendarState.collect { state ->
                        binding.textViewYearMonth.text = "${state.selectedYear}年${state.selectedMonth}月"
                        calendarAdapter.updateData(state.selectedYear, state.selectedMonth, state.selectedDay)
                        // 数据更新后再次校准高度
                        binding.root.post { updatePeekHeight() }
                    }
                }

                // 2. 【新增】监听小红点数据
                launch {
                    viewModel.daysWithNotes.collect { daySet ->
                        calendarAdapter.setDaysWithNotes(daySet)
                    }
                }

                // 3. 监听笔记列表数据
                launch {
                    viewModel.notePreviews.collectLatest { pagingData ->
                        notePreviewAdapter.submitData(pagingData)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}