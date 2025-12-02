package com.easynote.detail.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.data.entity.TagEntity

class TagPagingAdapter(
    private val selectedTags: HashSet<String>
) : PagingDataAdapter<TagEntity, TagPagingAdapter.TagViewHolder>(TAG_COMPARATOR) {

    inner class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvTagName)
        val ivIcon: ImageView = view.findViewById(R.id.ivTagIcon)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheck)

        init {
            view.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                item?.name?.let { tagName ->
                    if (selectedTags.contains(tagName)) {
                        selectedTags.remove(tagName)
                    } else {
                        selectedTags.add(tagName)
                    }
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_tag_single_item, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = getItem(position) // PagingAdapter 获取数据的方式

        if (item != null) {
            val tagName = item.name ?: "未命名"
            holder.tvName.text = tagName

            // --- 1. 获取颜色逻辑 (新增) ---
            // 优先用数据库里的 color 字段，如果为空或解析失败，就用默认灰色
            val colorStr = item.color
            val themeColor = try {
                if (!colorStr.isNullOrBlank()) {
                    android.graphics.Color.parseColor(colorStr)
                } else {
                    // 如果数据库没存颜色，可以用之前的 hash 逻辑生成一个，或者给个默认蓝
                    android.graphics.Color.parseColor("#2196F3")
                }
            } catch (e: IllegalArgumentException) {
                // 防止数据库存了 "red" 这种 parseColor 不认识的字串导致崩溃
                android.graphics.Color.GRAY
            }

            // --- 2. 设置图标颜色 ---
            // 无论选没选中，图标都显示它自己的代表色
            holder.ivIcon.setColorFilter(themeColor)

            // --- 3. 设置选中状态 (多选逻辑) ---
            if (selectedTags.contains(tagName)) {
                // 选中状态：显示勾选，文字变色
                holder.ivCheck.visibility = View.VISIBLE

                // 让文字颜色也变成标签色，强调选中
                holder.tvName.setTextColor(themeColor)
                // 加粗
                holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                // 未选中状态：隐藏勾选，文字黑色
                holder.ivCheck.visibility = View.GONE

                holder.tvName.setTextColor(android.graphics.Color.BLACK)
                holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
    
    companion object {
        private val TAG_COMPARATOR = object : DiffUtil.ItemCallback<TagEntity>() {
            override fun areItemsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem.id == newItem.id // 比较主键 ID
            }

            override fun areContentsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem == newItem // 比较内容
            }
        }
    }
}