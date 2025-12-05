package com.easynote.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class SwipeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var isMenuOpen = false
    // private set // 建议去掉 private set，或者确保 Adapter 只调用 close()

    init {
        overScrollMode = OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
    }

    // 这里其实不需要复杂的 onMeasure 了，因为 Adapter 已经强制设置了子 View 宽度。
    // 但保留着也没坏处，防止 Adapter 设置失败。

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            // 获取包裹菜单和内容的容器 (LinearLayout)
            val wrapper = getChildAt(0) as android.view.ViewGroup
            // 菜单是第二个子 View
            val menuView = wrapper.getChildAt(1)

            val menuWidth = menuView.width
            val currentScrollX = scrollX

            // 滑动超过菜单一半，展开；否则收起
            if (currentScrollX > menuWidth / 2) {
                this.smoothScrollTo(menuWidth, 0)
                isMenuOpen = true
            } else {
                this.smoothScrollTo(0, 0)
                isMenuOpen = false
            }
            return true
        }
        return super.onTouchEvent(ev)
    }

    fun close() {
        this.scrollTo(0, 0) // 使用 scrollTo 瞬间归位，比 smoothScrollTo 更适合复用重置
        isMenuOpen = false
    }
}