package com.easynote.util

import android.icu.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 【新增】日期时间工具类
 * 用于统一管理时间戳计算逻辑，供 Activity 和 ViewModel 复用
 */
object DateUtils {

    // 获取“今天”的起止时间戳
    fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val start = getStartOfDay(calendar)
        val end = getEndOfDay(calendar)
        return start to end
    }

    // 获取“昨天”的起止时间戳
    fun getYesterdayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val start = getStartOfDay(calendar)
        val end = getEndOfDay(calendar)
        return start to end
    }

    // 获取“本周”的起止时间戳 (周一为起点)
    fun getThisWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        // 设置周一为第一天
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = getStartOfDay(calendar)

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val end = getEndOfDay(calendar)
        return start to end
    }

    // 获取“本月”的起止时间戳
    fun getThisMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = getStartOfDay(calendar)

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val end = getEndOfDay(calendar)
        return start to end
    }

    // --- 私有辅助方法 ---
    private fun getStartOfDay(calendar: Calendar): Long {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(calendar: Calendar): Long {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    /**
     * 获取某年某月有多少天
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        // month - 1 因为 Calendar 的月份是 0-11
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * 根据年月日获取精准时间戳
     */
    fun getSpecificTimestamp(year: Int, month: Int, day: Int, isStartOfDay: Boolean): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        if (isStartOfDay) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
    /**
     * 判断指定的年月日是否是“今天”
     * @param month 1-based (1月是1)
     */
    fun isToday(year: Int, month: Int, day: Int): Boolean {
        val today = Calendar.getInstance()
        return today.get(Calendar.YEAR) == year &&
                (today.get(Calendar.MONTH) + 1) == month &&
                today.get(Calendar.DAY_OF_MONTH) == day
    }

    /**
     * 获取“某一天”的起止时间戳
     * 用于笔记筛选，比在循环里创建 Calendar 对象效率高得多
     * @param month 1-based
     */
    fun getDayRange(year: Int, month: Int, day: Int): Pair<Long, Long> {
        val start = getSpecificTimestamp(year, month, day, true)
        val end = getSpecificTimestamp(year, month, day, false)
        return start to end
    }
    /**
     * 🟢 [新增] 格式化时间戳为字符串 (给 NotePreviewViewHolder 用)
     * 统一 App 内的时间显示格式
     */
    fun formatDateTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }

    /**
     * 🟢 [新增] 获取某月第一天是星期几 (给 CalendarAdapter 用)
     * 返回: Calendar.SUNDAY (1) 到 Calendar.SATURDAY (7)
     */
    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }
    /**
     * 🟢 [智能时间格式化
     * 逻辑：
     * 1. 如果是今天 -> 显示 "上午/下午 HH:mm" (例如: 下午 14:30)
     * 2. 如果是当年但不是今天 -> 显示 "MM-dd" (例如: 12-03)
     * 3. 如果不是当年 -> 显示 "yyyy-MM-dd" (例如: 2024-12-03)
     */
    fun getSmartDate(timestamp: Long): String {
        val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()

        // 判断是否是同一年
        val isSameYear = targetCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)

        // 判断是否是同一天
        val isSameDay = isSameYear &&
                (targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR))

        return when {
            isSameDay -> {
                // 显示: 上午 10:30 (aa 代表上午/下午，HH:mm 代表 24小时制，hh:mm 代表 12小时制)
                // 如果你想用中文的 "上午/下午"，Locale.getDefault() 会自动处理
                val format = SimpleDateFormat("aa hh:mm", Locale.getDefault())
                format.format(Date(timestamp))
            }

            isSameYear -> {
                // 显示: 12-03
                val format = SimpleDateFormat("MM-dd", Locale.getDefault())
                format.format(Date(timestamp))
            }

            else -> {
                // 显示: 2023-11-25
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.format(Date(timestamp))
            }
        }
    }
}