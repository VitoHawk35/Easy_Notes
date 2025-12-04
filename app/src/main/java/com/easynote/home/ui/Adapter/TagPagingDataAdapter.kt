package com.easynote.home.ui.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.home.domain.model.TagModel

/**
 * 一个回调函数类型，用于处理标签项的点击事件。
 * @param TagModel 被点击的标签对象。
 */
typealias OnTagClickListener = (TagModel) -> Unit

/**
 * 用于在 RecyclerView 中展示分页加载的 TagModel 数据的 Adapter。
 * @param onTagClick 当一个标签被点击时将被调用的回调函数。
 */
class TagPagingDataAdapter(
    private val onTagClick: OnTagClickListener
) : PagingDataAdapter<TagModel, TagPagingDataAdapter.TagViewHolder>(TAG_COMPARATOR) {

    /**
     * 用于存储当前被选中的标签ID集合。
     * 这个属性应该由外部（例如 Activity/Fragment）根据 ViewModel 的状态来更新。
     * 当它的值被设置时，会刷新整个列表来更新UI。
     */
    var selectedTagIds: Set<Long> = emptySet()
        set(value) {
            if (field != value) {
                field = value
                // 使用 notifyDataSetChanged() 虽然简单，但在大数据集下效率不高。
                // 不过对于标签栏这种item数量不多的场景，是完全可以接受的。
                notifyDataSetChanged()
            }
        }

    /**
     * ViewHolder 负责持有并管理单个列表项的视图（item_tag_filter.xml）。
     */
    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 🟢 [新增] 获取新的 View 引用
        private val rootLayout: LinearLayout = itemView.findViewById(R.id.root_layout)
        private val colorDot: View = itemView.findViewById(R.id.view_tag_color)
        private val tagTextView: TextView = itemView.findViewById(R.id.textView_tag_name)

        init {
            itemView.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { tag ->
                    onTagClick(tag)
                }
            }
        }

        fun bind(tag: TagModel?) {
            tag?.let {
                tagTextView.text = it.tagName
                val isSelected = it.tagId in selectedTagIds

                // 1. 设置小圆点的颜色
                val tagColor = try {
                    Color.parseColor(it.color)
                } catch (e: Exception) {
                    Color.BLACK
                }
                // 🟢 [新增] 仅给小圆点染色
                val dotBackground = colorDot.background.mutate()
                dotBackground.setTint(Color.parseColor("#F5F5F5"))

                // 2. 处理背景选中状态
                // 🟡 [修改] 获取根布局背景
                val rootBackground = rootLayout.background.mutate()

                if (isSelected) {
                    // === 选中状态 ===
                    // 🟢 [修改] 背景变白
                    rootBackground.setTint(Color.WHITE)
                    // 🟢 [新增] 选中时给一个边框颜色(比如标签色)或者阴影，这里给一个淡淡的 Elevation 效果
                    rootLayout.elevation = 4f
                    dotBackground.setTint(tagColor)
                    // 字体保持黑色/深灰
                    tagTextView.setTextColor(Color.parseColor("#333333"))

                } else {
                    // === 未选中状态 ===
                    // 🟢 [修改] 背景变浅灰
                    rootBackground.setTint(Color.parseColor("#F5F5F5"))
                    rootLayout.elevation = 0f
                    //小圆点置灰消失
                    dotBackground.setTint(Color.parseColor("#F5F5F5"))
                    // 字体保持黑色/深灰
                    tagTextView.setTextColor(Color.parseColor("#666666"))
                }

                // 确保圆点可见
                colorDot.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 当 RecyclerView 需要一个新的 ViewHolder 时调用。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        // 加载你的 item_tag_filter.xml 布局文件
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_filter, parent, false)
        return TagViewHolder(view)
    }

    /**
     * 当 RecyclerView 需要在特定位置展示数据时调用。
     */
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        // 从 PagingData 中获取特定位置的数据项
        val tag = getItem(position)
        // 调用 ViewHolder 的 bind 方法来更新UI
        holder.bind(tag)
    }

    /**e
     * Companion object 用于存放静态成员，这里我们定义 DiffUtil.ItemCallback。
     * DiffUtil 是一个工具，能帮助 RecyclerView 高效地计算新旧数据列表之间的差异，
     * 只更新需要变化的部分，而不是刷新整个列表，从而实现流畅的动画和高性能的更新。
     * 这是 PagingDataAdapter 必须的。
     */
    companion object {
        private val TAG_COMPARATOR = object : DiffUtil.ItemCallback<TagModel>() {
            /**
             * 判断两个 item 是否是同一个对象（通常通过比较ID）。
             */
            override fun areItemsTheSame(oldItem: TagModel, newItem: TagModel): Boolean =
                oldItem.tagId == newItem.tagId

            /**
             * 判断两个 item 的内容是否完全相同。
             * 因为 TagModel 是一个 data class，它自动生成的 equals() 方法会比较所有属性，
             * 所以直接用 == 即可。
             */
            override fun areContentsTheSame(oldItem: TagModel, newItem: TagModel): Boolean =
                oldItem == newItem
        }
    }
}
