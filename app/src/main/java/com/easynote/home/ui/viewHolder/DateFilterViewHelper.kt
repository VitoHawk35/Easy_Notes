package com.easynote.home.ui

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.easynote.R
import com.easynote.databinding.DrawerHeaderDateFilterBinding
import com.easynote.util.DateUtils
import java.util.Calendar

/**
 * 视图助手：专门负责管理侧边栏那堆复杂的 Spinner 联动逻辑。
 * 它不包含业务逻辑，只负责 UI 交互和数据获取。
 */
class DateFilterViewHelper(
    private val context: Context,
    private val binding: DrawerHeaderDateFilterBinding
) {

    fun setup() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // 🟢 [优化] 使用自定义的紧凑布局 item_spinner_compact
        val yearAdapter = ArrayAdapter(context, R.layout.item_spinner_compact, (currentYear - 10..currentYear).toList().reversed())
        val monthAdapter = ArrayAdapter(context, R.layout.item_spinner_compact, (1..12).toList())

        // 设置下拉弹出后的样式，也是用紧凑布局
        yearAdapter.setDropDownViewResource(R.layout.item_spinner_compact)
        monthAdapter.setDropDownViewResource(R.layout.item_spinner_compact)

        // 绑定 Adapter
        binding.spinnerStartYear.adapter = yearAdapter
        binding.spinnerEndYear.adapter = yearAdapter
        binding.spinnerStartMonth.adapter = monthAdapter
        binding.spinnerEndMonth.adapter = monthAdapter

        // 设置联动监听 (年/月变化 -> 更新天数)
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDaySpinners()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        listOf(
            binding.spinnerStartYear, binding.spinnerStartMonth,
            binding.spinnerEndYear, binding.spinnerEndMonth
        ).forEach { it.onItemSelectedListener = listener }

        // 初始化默认值
        binding.spinnerStartYear.setSelection(0)
        binding.spinnerEndYear.setSelection(0)
        binding.spinnerStartMonth.setSelection(0)
        binding.spinnerEndMonth.setSelection(currentMonth - 1)

        // 初始化天数
        updateDaySpinners()

        // 选中当天
        if (binding.spinnerEndDay.adapter.count >= currentDay) {
            binding.spinnerEndDay.setSelection(currentDay - 1)
        }
    }

    /**
     * 🟢 [新增] 核心功能：获取当前用户选择的时间范围
     * @return Pair(开始时间戳, 结束时间戳)。如果选择无效（如某些未加载），返回 null。
     */
    fun getSelectedDateRange(): Pair<Long, Long>? {
        val startTime = getTimestamp(
            binding.spinnerStartYear, binding.spinnerStartMonth, binding.spinnerStartDay, true
        )
        val endTime = getTimestamp(
            binding.spinnerEndYear, binding.spinnerEndMonth, binding.spinnerEndDay, false
        )

        if (startTime != null && endTime != null) {
            return startTime to endTime
        }
        return null
    }

    private fun updateDaySpinners() {
        updateSingleDaySpinner(binding.spinnerStartYear, binding.spinnerStartMonth, binding.spinnerStartDay)
        updateSingleDaySpinner(binding.spinnerEndYear, binding.spinnerEndMonth, binding.spinnerEndDay)
    }

    private fun updateSingleDaySpinner(yearSpinner: Spinner, monthSpinner: Spinner, daySpinner: Spinner) {
        val year = yearSpinner.selectedItem as? Int ?: return
        val month = monthSpinner.selectedItem as? Int ?: return

        val daysInMonth = DateUtils.getDaysInMonth(year, month)

        // 🟢 [优化] 天数也使用紧凑布局
        val dayAdapter = ArrayAdapter(context, R.layout.item_spinner_compact, (1..daysInMonth).toList())
        dayAdapter.setDropDownViewResource(R.layout.item_spinner_compact)

        val currentSelection = daySpinner.selectedItemPosition
        daySpinner.adapter = dayAdapter

        // 保持选中状态
        if (currentSelection in 0 until daysInMonth) {
            daySpinner.setSelection(currentSelection)
        } else {
            daySpinner.setSelection(0)
        }
    }

    private fun getTimestamp(yearS: Spinner, monthS: Spinner, dayS: Spinner, isStart: Boolean): Long? {
        val year = yearS.selectedItem as? Int
        val month = monthS.selectedItem as? Int
        val day = dayS.selectedItem as? Int
        if (year != null && month != null && day != null) {
            return DateUtils.getSpecificTimestamp(year, month, day, isStart)
        }
        return null
    }
}