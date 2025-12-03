package com.easynote.home.ui.Adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.databinding.ItemCalendarDayBinding
import com.easynote.home.domain.model.NotePreviewModel
import java.util.Calendar

/**
 * 日历项目密封类
 */
sealed class CalendarItem {
    data class WeekTitle(val title: String) : CalendarItem()
    data class Day(val dayOfMonth: Int, val belongsToCurrentMonth: Boolean) : CalendarItem()
    object Blank : CalendarItem()
}

class CalendarAdapter(
    private val onDateClick: (day: Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var year: Int = 0
    private var month: Int = 0 // 1-12
    private var selectedDay: Int? = null

    // 【核心数据源】
    // Key: 日期 (dayOfMonth)
    // Value: 当天的笔记列表 (NotePreviewModel)
    private var dailyData: Map<Int, List<NotePreviewModel>> = emptyMap()

    private val displayItems = mutableListOf<CalendarItem>()
    private val today = Calendar.getInstance()

    // --- 更新方法 ---

    /**
     * 更新基础日历结构（年份、月份、选中日期变化时调用）
     */
    fun updateData(year: Int, month: Int, selectedDay: Int?) {
        this.year = year
        this.month = month
        this.selectedDay = selectedDay
        regenerateDisplayItems()
        notifyDataSetChanged()
    }

    /**
     * 更新每一天的笔记数据（当 ViewModel 获取到本月笔记时调用）
     * 只刷新日期格子区域，避免整个列表闪烁
     */
    fun updateDailyData(data: Map<Int, List<NotePreviewModel>>) {
        this.dailyData = data
        // 假设前7个是周标题 (0-6)，从第7个 (下标7) 开始是具体的格子
        if (itemCount > 7) {
            notifyItemRangeChanged(7, itemCount - 7)
        }
    }

    private fun regenerateDisplayItems() {
        displayItems.clear()
        val calendar = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 7=Saturday

        // 1. 添加星期标题
        WEEK_TITLES.forEach { displayItems.add(CalendarItem.WeekTitle(it)) }

        // 2. 添加月初的空白格
        (1 until firstDayOfWeek).forEach { displayItems.add(CalendarItem.Blank) }

        // 3. 添加当月的所有日期
        (1..daysInMonth).forEach { displayItems.add(CalendarItem.Day(it, true)) }
    }

    // --- RecyclerView 必须实现的方法 ---

    override fun getItemCount(): Int = displayItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(displayItems[position])
    }

    // --- ViewHolder ---

    inner class CalendarViewHolder(val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val defaultTextColor: Int
        private val secondaryTextColor: Int
        private val primaryColor: Int

        init {
            val context = itemView.context
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            defaultTextColor = ContextCompat.getColor(context, typedValue.resourceId)
            context.theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
            secondaryTextColor = ContextCompat.getColor(context, typedValue.resourceId)
            context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            primaryColor = typedValue.data
        }

        fun bind(item: CalendarItem) {
            val context = binding.root.context
            val layoutParams = binding.root.layoutParams

            // 1. 动态设置高度
            if (item is CalendarItem.WeekTitle) {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                // 日期格子高度设为 65dp，以确保 6 行日历也能在大多数屏幕上显示完整
                val heightInPx = (65 * context.resources.displayMetrics.density).toInt()
                layoutParams.height = heightInPx
            }
            binding.root.layoutParams = layoutParams

            // 2. 重置视图状态 (防止复用导致的显示残留)
            binding.root.visibility = View.VISIBLE
            binding.textViewDay.text = ""
            binding.root.isClickable = false
            binding.root.isActivated = false

            // 默认隐藏所有预览和红点
            binding.textViewPreview1.visibility = View.GONE
            binding.textViewPreview2.visibility = View.GONE
            binding.dotHasNotes.visibility = View.GONE

            // 恢复背景
            binding.root.setBackgroundResource(R.drawable.selector_calendar_day)

            // 3. 根据类型填充数据
            when (item) {
                is CalendarItem.WeekTitle -> {
                    binding.textViewDay.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    binding.textViewDay.text = item.title
                    binding.textViewDay.setTextColor(secondaryTextColor)
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }

                is CalendarItem.Blank -> {
                    binding.root.visibility = View.INVISIBLE
                }

                is CalendarItem.Day -> {
                    if (!item.belongsToCurrentMonth) {
                        binding.root.visibility = View.INVISIBLE
                        return
                    }

                    // 设置日期数字
                    binding.textViewDay.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    binding.textViewDay.text = item.dayOfMonth.toString()
                    binding.root.isClickable = true

                    // 选中状态
                    binding.root.isActivated = (item.dayOfMonth == selectedDay)

                    val isToday = (year == today.get(Calendar.YEAR) &&
                            month == today.get(Calendar.MONTH) + 1 &&
                            item.dayOfMonth == today.get(Calendar.DAY_OF_MONTH))

                    // 字体颜色逻辑
                    when {
                        binding.root.isActivated -> binding.textViewDay.setTextColor(Color.WHITE)
                        isToday -> binding.textViewDay.setTextColor(primaryColor)
                        else -> binding.textViewDay.setTextColor(defaultTextColor)
                    }

                    // =========================================================
                    // 【核心】显示笔记预览和红点逻辑
                    // =========================================================
                    val notes = dailyData[item.dayOfMonth] ?: emptyList()

                    // 1. 第一条标题
                    if (notes.isNotEmpty()) {
                        binding.textViewPreview1.visibility = View.VISIBLE
                        binding.textViewPreview1.text = notes[0].title.ifEmpty { "无标题" }
                    }

                    // 2. 第二条标题
                    if (notes.size > 1) {
                        binding.textViewPreview2.visibility = View.VISIBLE
                        binding.textViewPreview2.text = notes[1].title.ifEmpty { "无标题" }
                    }

                    // 3. 小红点逻辑
                    // 逻辑 A: 只有超过 2 条笔记时才显示红点 (表示还有更多)
                    if (notes.size > 2) {
                        binding.dotHasNotes.visibility = View.VISIBLE
                    }

                    /*
                    // 逻辑 B: 只要有笔记就显示红点 (如果你更喜欢这种)
                    if (notes.isNotEmpty()) {
                        binding.dotHasNotes.visibility = View.VISIBLE
                    }
                    */

                    // 点击事件
                    binding.root.setOnClickListener { onDateClick(item.dayOfMonth) }
                }
            }
        }
    }

    companion object {
        private val WEEK_TITLES = listOf("日", "一", "二", "三", "四", "五", "六")
    }
}