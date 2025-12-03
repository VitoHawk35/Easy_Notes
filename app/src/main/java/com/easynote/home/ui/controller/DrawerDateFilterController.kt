package com.easynote.home.ui

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.easynote.databinding.DrawerHeaderDateFilterBinding
import com.easynote.util.DateUtils // 复用 DateUtils
import java.util.Calendar

/**
 * 专门用于管理侧边栏日期筛选 UI 的控制器。
 * 将这部分臃肿的逻辑从 HomeActivity 中剥离。
 */
class DrawerDateFilterController(
    private val context: Context,
    private val binding: DrawerHeaderDateFilterBinding,
    private val viewModel: HomeViewModel,
    private val onApplyOrCancel: () -> Unit // 回调：通知 Activity 关闭侧边栏
) {

    fun setup() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // 初始化 Spinner 的 Adapters
        val yearAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, (currentYear - 10..currentYear).toList().reversed())
        val monthAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, (1..12).toList())
        yearAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        monthAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        binding.spinnerStartYear.adapter = yearAdapter
        binding.spinnerEndYear.adapter = yearAdapter
        binding.spinnerStartMonth.adapter = monthAdapter
        binding.spinnerEndMonth.adapter = monthAdapter

        // 设置 Spinner 监听器
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDaySpinners()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerStartYear.onItemSelectedListener = listener
        binding.spinnerStartMonth.onItemSelectedListener = listener
        binding.spinnerEndYear.onItemSelectedListener = listener
        binding.spinnerEndMonth.onItemSelectedListener = listener

        // 初始化默认日期
        binding.spinnerStartYear.setSelection(0)
        binding.spinnerEndYear.setSelection(0)
        binding.spinnerStartMonth.setSelection(0) // 1月
        binding.spinnerEndMonth.setSelection(currentMonth - 1)

        // 先更新天数 adapter，再设置选中项
        updateDaySpinners()

        // 安全设置天的选中项
        if (binding.spinnerEndDay.adapter.count >= currentDay) {
            binding.spinnerEndDay.setSelection(currentDay - 1)
        }

        // 设置按钮点击事件
        binding.buttonApplyDateFilter.setOnClickListener {
            val startTimeStamp = getSelectedTimeStamp(
                binding.spinnerStartYear,
                binding.spinnerStartMonth,
                binding.spinnerStartDay,
                isStartOfDay = true
            )

            val endTimeStamp = getSelectedTimeStamp(
                binding.spinnerEndYear,
                binding.spinnerEndMonth,
                binding.spinnerEndDay,
                isStartOfDay = false
            )

            viewModel.applyDateFilter(startTimeStamp, endTimeStamp)
            onApplyOrCancel() // 关闭侧边栏
        }

        binding.buttonClearDateFilter.setOnClickListener {
            viewModel.clearDateFilter()
            onApplyOrCancel() // 关闭侧边栏
        }
    }

    /**
     * 复用 DateUtils 计算某个月有多少天，更新 Spinner
     */
    private fun updateDaySpinners() {
        updateDaySpinner(binding.spinnerStartYear, binding.spinnerStartMonth, binding.spinnerStartDay)
        updateDaySpinner(binding.spinnerEndYear, binding.spinnerEndMonth, binding.spinnerEndDay)
    }

    private fun updateDaySpinner(yearSpinner: Spinner, monthSpinner: Spinner, daySpinner: Spinner) {
        val year = yearSpinner.selectedItem as? Int ?: return
        val month = monthSpinner.selectedItem as? Int ?: return

        // 【复用 DateUtils】逻辑
        val daysInMonth = DateUtils.getDaysInMonth(year, month)

        val dayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, (1..daysInMonth).toList())
        dayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        // 尝试保持之前选中的 index
        val currentSelection = daySpinner.selectedItemPosition
        daySpinner.adapter = dayAdapter
        if (currentSelection in 0 until daysInMonth) {
            daySpinner.setSelection(currentSelection)
        }
    }

    /**
     * 复用 DateUtils 计算时间戳
     */
    private fun getSelectedTimeStamp(
        yearSpinner: Spinner,
        monthSpinner: Spinner,
        daySpinner: Spinner,
        isStartOfDay: Boolean
    ): Long? {
        val year = yearSpinner.selectedItem as? Int
        val month = monthSpinner.selectedItem as? Int
        val day = daySpinner.selectedItem as? Int

        if (year != null && month != null && day != null) {
            // 【复用 DateUtils】逻辑
            return DateUtils.getSpecificTimestamp(year, month, day, isStartOfDay)
        }
        return null
    }
}