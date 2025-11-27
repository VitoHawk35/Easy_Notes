package com.easynote.home.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mydemo.R // 确保 R 文件被正确导入
import com.example.easynote.domain.model.NotePreviewModel

/**
 * 这是连接 PagingData<NotePreviewModel> 数据和 RecyclerView 的 Adapter。
 */
class NotePreviewPagingAdapter : PagingDataAdapter<NotePreviewModel, NotePreviewPagingAdapter.NotePreviewViewHolder>(NOTE_COMPARATOR) {

    /**
     * ViewHolder 是一个“视图持有者”。
     * 它的作用是缓存 item_note_preview.xml 布局中的视图（TextView, ImageView等），
     * 避免了每次在列表中滚动时都去调用 findViewById() 这个耗性能的操作。
     */
    class NotePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 1. 在这里找到布局中的所有视图，并把它们存为属性
        private val titleTextView: TextView = itemView.findViewById(R.id.textView_note_title)
        private val summaryTextView: TextView = itemView.findViewById(R.id.textView_note_summary)
        private val timeTextView: TextView = itemView.findViewById(R.id.textView_note_time)
        private val pinnedImageView: ImageView = itemView.findViewById(R.id.imageView_pinned)

        /**
         * 这是一个将数据绑定到视图上的方法。
         * onBindViewHolder 会调用这个方法。
         */
        fun bind(note: NotePreviewModel?) {
            // 安全地处理可能为 null 的情况（在占位符启用时可能发生）
            note?.let {
                // 2. 将数据设置到对应的视图上
                titleTextView.text = it.title
                summaryTextView.text = it.summary
                // 这里只是一个示例，实际项目中你需要一个函数来格式化时间戳
                timeTextView.text = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it.updatedTime))

                // 3. 根据 isPinned 状态，决定是否显示“置顶”图标
                if (it.isPinned) {
                    pinnedImageView.visibility = View.VISIBLE
                } else {
                    pinnedImageView.visibility = View.GONE
                }
            }
        }
    }

    /**
     * 当 RecyclerView 需要创建一个新的 ViewHolder 时，这个方法会被调用。
     * （例如，当列表首次加载或滚动出新的、需要复用的视图时）
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePreviewViewHolder {
        // 1. 加载（Inflate）你的 item_note_preview.xml 布局文件
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_preview, parent, false)

        // 2. 用这个加载好的 view 创建一个 ViewHolder 实例并返回
        return NotePreviewViewHolder(view)
    }

    /**
     * 当 RecyclerView 需要在一个特定的位置显示数据时，这个方法会被调用。
     * 它的职责就是从数据集中取出数据，并将其绑定到对应的 ViewHolder 上。
     */
    override fun onBindViewHolder(holder: NotePreviewViewHolder, position: Int) {
        // 1. 从 PagingData 中获取当前位置的数据项
        val note = getItem(position)
        // 2. 调用 ViewHolder 的 bind 方法，让它去更新自己的UI
        holder.bind(note)
    }

    /**
     * Companion object 用于存放类的静态 成员。
     * DiffUtil 是 Paging 3 的核心，它帮助 RecyclerView 高效地计算新旧数据列表之间的差异，
     * 只更新需要变化的部分，从而实现流畅的动画和高性能的更新。
     * 这是 PagingDataAdapter 必须的。
     */
    companion object {
        private val NOTE_COMPARATOR = object : DiffUtil.ItemCallback<NotePreviewModel>() {
            /**
             * 判断两个 item 是否是同一个对象（通常通过比较唯一ID）。
             * 如果返回 true，RecyclerView 就知道这是同一个 item，可能会更新它的内容。
             */
            override fun areItemsTheSame(oldItem: NotePreviewModel, newItem: NotePreviewModel): Boolean =
                oldItem.noteId == newItem.noteId

            /**
             * 当 areItemsTheSame 返回 true 时，这个方法才会被调用。
             * 它用来判断这个 item 的内容是否发生了变化。
             * 因为 NotePreviewModel 是一个 data class，它自动生成的 equals() 方法会比较所有属性，所以直接用 == 即可。
             * 如果返回 false，onBindViewHolder 就会被调用来更新UI。
             */
            override fun areContentsTheSame(oldItem: NotePreviewModel, newItem: NotePreviewModel): Boolean =
                oldItem == newItem
        }
    }
}
