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
    private val selectedTags: HashSet<TagEntity>
) : PagingDataAdapter<TagEntity, TagPagingAdapter.TagViewHolder>(TAG_COMPARATOR) {

    inner class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvTagName)
        val ivIcon: ImageView = view.findViewById(R.id.ivTagIcon)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheck)

        init {
            view.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if (item != null) {
                    if (containsTag(item)) {
                        removeTag(item)
                    } else {
                        selectedTags.add(item)
                    }
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    private fun containsTag(target: TagEntity): Boolean {
        return selectedTags.any { it.id == target.id }
    }

    private fun removeTag(target: TagEntity) {
        val iterator = selectedTags.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == target.id) {
                iterator.remove()
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_tag_single_item, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            val tagName = item.name ?: "未命名"
            holder.tvName.text = tagName

            val colorStr = item.color
            val themeColor = try {
                if (!colorStr.isNullOrBlank()) {
                    android.graphics.Color.parseColor(colorStr)
                } else {
                    android.graphics.Color.parseColor("#2196F3")
                }
            } catch (e: IllegalArgumentException) {
                android.graphics.Color.GRAY
            }

            holder.ivIcon.setColorFilter(themeColor)

            if (containsTag(item)) {
                holder.ivCheck.visibility = View.VISIBLE
                holder.tvName.setTextColor(themeColor) // 选中变色
                holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                holder.ivCheck.visibility = View.GONE
                holder.tvName.setTextColor(Color.BLACK) // 未选中黑色
                holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
    
    companion object {
        private val TAG_COMPARATOR = object : DiffUtil.ItemCallback<TagEntity>() {
            override fun areItemsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}