package com.easynote.home.ui.Adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easynote.databinding.ItemTagManagementBinding
import com.easynote.home.domain.model.TagModel

class TagListAdapter(
    private val onEditClick: (TagModel) -> Unit,
    private val onDeleteClick: (TagModel) -> Unit
) : ListAdapter<TagModel, TagListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemTagManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTagManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 🟢【核心修复】
        // 获取屏幕宽度
        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // 计算两侧的 Margin 总和 (XML中设置了 marginHorizontal="16dp"，所以总共是 32dp)
        // 将 32dp 转换为像素
        val density = displayMetrics.density
        val marginPixels = (32 * density).toInt()

        // 设置内容区域宽度 = 屏幕宽度 - 边距
        val params = binding.contentLayout.layoutParams
        params.width = screenWidth - marginPixels
        binding.contentLayout.layoutParams = params

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // 视图复用时重置滑动状态
        holder.binding.swipeLayout.scrollTo(0, 0)
        holder.binding.swipeLayout.close()

        holder.binding.textViewTagName.text = item.tagName

        // 设置颜色
        val drawable = holder.binding.viewTagColor.background as GradientDrawable
        try {
            drawable.setColor(Color.parseColor(item.color))
        } catch (e: Exception) {
            drawable.setColor(Color.GRAY)
        }

        // 1. 点击内容区域 -> 修改
        holder.binding.contentLayout.setOnClickListener {
            if (holder.binding.swipeLayout.scrollX > 0) {
                holder.binding.swipeLayout.close()
            } else {
                onEditClick(item)
            }
        }

        // 2. 点击红色按钮 -> 删除
        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(item)
            holder.binding.swipeLayout.close()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TagModel>() {
            override fun areItemsTheSame(oldItem: TagModel, newItem: TagModel) = oldItem.tagId == newItem.tagId
            override fun areContentsTheSame(oldItem: TagModel, newItem: TagModel) = oldItem == newItem
        }
    }
}