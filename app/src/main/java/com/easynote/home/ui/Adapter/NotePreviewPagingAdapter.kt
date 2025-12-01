package com.easynote.home.ui.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R // 确保 R 文件被正确导入
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.ui.HomeUiMode
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 这是连接 PagingData<NotePreviewModel> 数据和 RecyclerView 的 Adapter。
 */
class NotePreviewPagingAdapter(
    private val onItemClick: (NotePreviewModel) -> Unit,
    private val onItemLongClick: (NotePreviewModel) -> Boolean
) : PagingDataAdapter<NotePreviewModel, NotePreviewPagingAdapter.NotePreviewViewHolder>(NOTE_COMPARATOR) {

    /**
     * ViewHolder 是一个“视图持有者”。
     * 它的作用是缓存 item_note_preview.xml 布局中的视图（TextView, ImageView等），
     * 避免了每次在列表中滚动时都去调用 findViewById() 这个耗性能的操作。
     */
    var currentUiMode: HomeUiMode = HomeUiMode.Browsing
        set(value) {
            if (field != value) { // 只有在值真正改变时才刷新
                field = value
                // 只通知可见范围内的 Item 需要更新，而不是所有数据都变了
                notifyItemRangeChanged(0, itemCount)
            }
        }

    inner class NotePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 1. 在这里找到单个笔记预览中的所有视图，并把它们存为属性
        private val titleTextView: TextView = itemView.findViewById(R.id.textView_note_title)
        private val summaryTextView: TextView = itemView.findViewById(R.id.textView_note_summary)
        private val timeTextView: TextView = itemView.findViewById(R.id.textView_note_time)
        private val pinnedImageView: ImageView = itemView.findViewById(R.id.imageView_pinned)
        private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox_selection)


        // 在 ViewHolder 初始化时就设置好监听器
        init {
            // 设置单击事件监听器
            itemView.setOnClickListener {
                // a. 先安全地获取当前位置的数据项
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { note ->
                        // b. 检查当前的UI模式
                        when (currentUiMode) {
                            is HomeUiMode.Browsing -> {
                                // 在浏览模式下，执行“单击”回调 (例如，进入详情页)
                                onItemClick(note)
                            }
                            is HomeUiMode.Managing -> {
                                // 在管理模式下，执行“长按”回调 (切换选中状态)
                                // 这里我们复用 onLongClick 的逻辑，因为它也需要切换选中
                                // 为了更清晰，我们应该在Fragment中调用ViewModel的toggleSelection方法
                                // 实际上，这里的单击也应该有一个单独的回调，或者让外部处理
                                // 但为了简单，我们直接复用 onLongClick 回调的逻辑
                                // 更好的做法是让外部的 onItemClick 自己去判断模式
                                onItemClick(note) // << 让外部的 onItemClick 自己判断
                            }
                        }
                    }
                }
            }

            // 设置长按事件监听器
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { note ->
                        // 【关键逻辑】只在浏览模式下，长按才有效
                        if (currentUiMode is HomeUiMode.Browsing) {
                            // 调用外部传入的 onItemLongClick 回调函数，并返回它的结果
                            return@setOnLongClickListener onItemLongClick(note)
                        }
                    }
                }
                // 在管理模式下，长按事件不被消费，或者你可以返回 true 让它不触发任何其他效果
                false
            }
        }

        /**
         * 这是一个将数据绑定到视图上的方法。
         * onBindViewHolder 会调用这个方法。
         */
        fun bind(note: NotePreviewModel?,mode: HomeUiMode) {
            // 安全地处理可能为 null 的情况（在占位符启用时可能发生）
            note?.let {
                // 2. 将数据设置到对应的视图上
                titleTextView.text = it.title
                summaryTextView.text = it.summary
                // 这里只是一个示例，实际项目中你需要一个函数来格式化时间戳
                timeTextView.text = SimpleDateFormat("yyyy-MM-dd").format(Date(it.updatedTime))
                // 3. 根据 isPinned 状态，决定是否显示“置顶”图标
                if (it.isPinned) {
                    pinnedImageView.visibility = View.VISIBLE
                } else {
                    pinnedImageView.visibility = View.GONE
                }
                when (mode) {
                    is HomeUiMode.Browsing -> {
                        // 浏览模式下，隐藏复选框
                        selectionCheckbox.visibility = View.GONE
                    }
                    is HomeUiMode.Managing -> {
                        // 管理模式下，显示复选框
                        selectionCheckbox.visibility = View.VISIBLE
                        // 根据ID是否在选中集合中，来设置复选框的选中状态
                        selectionCheckbox.isChecked = it.noteId in mode.allSelectedIds
                    }
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
        holder.bind(note,currentUiMode)
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
