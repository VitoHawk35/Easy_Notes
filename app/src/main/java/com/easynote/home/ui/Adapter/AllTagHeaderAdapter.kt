package com.easynote.home.ui.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R

class AllTagHeaderAdapter(
    private val onAllTagClick: () -> Unit // 用于处理点击事件的回调
) : RecyclerView.Adapter<AllTagHeaderAdapter.AllTagViewHolder>() {

    var isSelected: Boolean = true // 用于控制“全部”按钮的选中状态
        set(value) {
            field = value
            notifyItemChanged(0) // 当状态改变时，通知刷新UI
        }

    inner class AllTagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 🟢 [新增] 获取 View 引用
        private val rootLayout: LinearLayout = itemView.findViewById(R.id.root_layout)
        private val colorDot: View = itemView.findViewById(R.id.view_tag_color)
        private val allTagText: TextView = itemView.findViewById(R.id.textView_tag_name)

        fun bind() {
            allTagText.text = "全部"

            // 🟢 [新增] "全部"按钮不需要圆点，隐藏它
            colorDot.visibility = View.GONE

            val background = rootLayout.background.mutate()

            if (isSelected) {
                // === 选中状态 ===
                // 🟢 [修改] 背景变白
                background.setTint(Color.WHITE)
                // 🟢 [新增] 阴影
                rootLayout.elevation = 4f

                allTagText.setTextColor(Color.parseColor("#333333"))
            } else {
                // === 未选中状态 ===
                // 🟢 [修改] 背景变浅灰
                background.setTint(Color.parseColor("#F5F5F5"))
                rootLayout.elevation = 0f

                allTagText.setTextColor(Color.parseColor("#666666"))
            }
        }

        init {
            itemView.setOnClickListener {
                onAllTagClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllTagViewHolder {
        // 复用你的 item_tag_filter.xml 布局
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_filter, parent, false)
        return AllTagViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllTagViewHolder, position: Int) {
        holder.bind()
    }

    // 这个Adapter永远只有一个item
    override fun getItemCount(): Int = 1
}
