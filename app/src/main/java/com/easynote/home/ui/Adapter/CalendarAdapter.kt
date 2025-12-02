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
import java.util.Calendar

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

    // 【新增】存储有笔记的日期 (几号)
    private var daysWithNotes: Set<Int> = emptySet()

    private val displayItems = mutableListOf<CalendarItem>()
    private val today = Calendar.getInstance()

    fun updateData(year: Int, month: Int, selectedDay: Int?) {
        this.year = year
        this.month = month
        this.selectedDay = selectedDay
        regenerateDisplayItems()
        notifyDataSetChanged()
    }

    // 【新增】更新小红点数据
    fun setDaysWithNotes(days: Set<Int>) {
        this.daysWithNotes = days
        // 刷新日历格子 (假设前7个是周标题，从第7个开始刷新)
        // 这样可以避免刷新整个列表带来的闪烁
        notifyItemRangeChanged(7, itemCount - 7)
    }

    private fun regenerateDisplayItems() {
        displayItems.clear()
        val calendar = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday

        // 1. 星期标题
        WEEK_TITLES.forEach { displayItems.add(CalendarItem.WeekTitle(it)) }
        // 2. 空白
        (1 until firstDayOfWeek).forEach { displayItems.add(CalendarItem.Blank) }
        // 3. 日期
        (1..daysInMonth).forEach { displayItems.add(CalendarItem.Day(it, true)) }
    }

    override fun getItemCount(): Int = displayItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(displayItems[position])
    }

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

            // 高度设置：标题自适应，格子固定高度
            if (item is CalendarItem.WeekTitle) {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                // 【建议】设为 65dp 或 70dp 以适应小屏幕展示6行日历
                val heightInPx = (65 * context.resources.displayMetrics.density).toInt()
                layoutParams.height = heightInPx
            }
            binding.root.layoutParams = layoutParams

            // 重置状态
            binding.root.visibility = View.VISIBLE
            binding.textViewDay.text = ""
            binding.root.isClickable = false
            binding.root.isActivated = false
            binding.dotHasNotes.visibility = View.GONE // 默认隐藏红点
            binding.root.setBackgroundResource(R.drawable.selector_calendar_day)

            when (item) {
                is CalendarItem.WeekTitle -> {
                    binding.textViewDay.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    binding.textViewDay.text = item.title
                    binding.textViewDay.setTextColor(secondaryTextColor)
                    binding.root.isClickable = false
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }

                is CalendarItem.Blank -> {
                    binding.root.visibility = View.INVISIBLE
                }

                is CalendarItem.Day -> {
                    binding.textViewDay.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    binding.textViewDay.text = item.dayOfMonth.toString()
                    binding.root.isClickable = true
                    binding.root.isActivated = (item.dayOfMonth == selectedDay)

                    val isToday = (year == today.get(Calendar.YEAR) &&
                            month == today.get(Calendar.MONTH) + 1 &&
                            item.dayOfMonth == today.get(Calendar.DAY_OF_MONTH))

                    when {
                        binding.root.isActivated -> binding.textViewDay.setTextColor(Color.WHITE)
                        isToday -> binding.textViewDay.setTextColor(primaryColor)
                        else -> binding.textViewDay.setTextColor(defaultTextColor)
                    }

                    // 【核心】判断是否显示小红点
                    if (daysWithNotes.contains(item.dayOfMonth)) {
                        binding.dotHasNotes.visibility = View.VISIBLE
                    }

                    binding.root.setOnClickListener { onDateClick(item.dayOfMonth) }
                }
            }
        }
    }

    companion object {
        private val WEEK_TITLES = listOf("日", "一", "二", "三", "四", "五", "六")
    }
}