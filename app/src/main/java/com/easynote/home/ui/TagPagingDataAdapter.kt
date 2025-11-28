package com.easynote.home.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // 从布局中找到 TextView
        private val tagTextView: TextView = itemView.findViewById(R.id.textView_tag_name)

        init {
            // 在 ViewHolder 创建时就设置好点击监听器，这是最高效的做法。
            itemView.setOnClickListener {
                // getItem(bindingAdapterPosition) 是一个安全的方式来获取当前位置的数据。
                // 如果位置有效，就调用外部传入的 onTagClick 回调函数。
                getItem(bindingAdapterPosition)?.let { tag ->
                    onTagClick(tag)
                }
            }
        }

        /**
         * 将一个 TagModel 数据绑定到视图上，并根据选中状态更新UI。
         * @param tag 要显示的数据对象。
         */
        fun bind(tag: TagModel?) {
            tag?.let {
                // 设置标签名称
                tagTextView.text = it.tagName

                // 根据 tagId 是否在 selectedTagIds 集合中，来更新UI的选中样式
                if (it.tagId in selectedTagIds) {
                    // 设置为选中样式，例如，不透明
                    itemView.alpha = 1.0f
                } else {
                    // 设置为未选中样式，例如，半透明
                    itemView.alpha = 0.6f
                }
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
