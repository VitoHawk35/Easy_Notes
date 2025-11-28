package com.easynote.home.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private val allTagButton: TextView = itemView.findViewById(R.id.textView_tag_name)

        fun bind() {
            allTagButton.text = "全部"
            // 根据isSelected状态来改变UI样式
            // 例如，改变背景或文字颜色
            if (isSelected) {
                // 设置为选中样式
                itemView.alpha = 1.0f
            } else {
                // 设置为未选中样式
                itemView.alpha = 0.5f // 举例：半透明
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
