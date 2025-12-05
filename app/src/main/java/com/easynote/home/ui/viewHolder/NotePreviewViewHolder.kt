package com.easynote.home.ui.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.ui.HomeUiMode
import com.easynote.home.ui.SortOrder
import com.easynote.util.DateUtils
import com.google.android.material.card.MaterialCardView
/**
 * 【公共的笔记预览ViewHolder，供首页和日历页复用】
 * 独立的 ViewHolder，负责持有 View 和绑定数据逻辑。
 * 可以被 PagingAdapter 和 ListAdapter 复用。
 */
class NotePreviewViewHolder(
    itemView: View,
    private val onItemClick: (NotePreviewModel) -> Unit,
    private val onItemLongClick: (NotePreviewModel) -> Boolean?,
    private val getUiMode: () -> HomeUiMode, // 通过函数获取当前的 UI Mode，解耦
    private val getSortOrder: () -> SortOrder
) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView: TextView = itemView.findViewById(R.id.textView_note_title)
    private val summaryTextView: TextView = itemView.findViewById(R.id.textView_note_summary)
    private val timeTextView: TextView = itemView.findViewById(R.id.textView_note_time)
    private val pinnedImageView: ImageView = itemView.findViewById(R.id.imageView_pinned)
    private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox_selection)
    // 获取根布局 CardView
    private val cardView: MaterialCardView = itemView as MaterialCardView
    // 定义颜色
    // 选中后的背景色：比背景色(#E0E0E0)稍深一点的灰色，例如 #D0D0D0
    private val selectedColor = Color.parseColor("#D0D0D0")
    // 默认背景色：白色
    private val defaultColor = Color.WHITE
    // 当前绑定的数据项，用于点击事件
    private var currentNote: NotePreviewModel? = null

    init {
        // 单击事件
        itemView.setOnClickListener {
            bindingAdapterPosition.let { position ->
                if (position != RecyclerView.NO_POSITION) {
                    currentNote?.let(onItemClick)
                }
            }
        }

        // 长按事件
        onItemLongClick.let { longClickCallback ->
            itemView.setOnLongClickListener {
                bindingAdapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        currentNote?.let { note ->
                            if (getUiMode() is HomeUiMode.Browsing) {
                                return@setOnLongClickListener longClickCallback(note) == true
                            }
                        }
                    }
                }
                false
            }
        }
    }

    /**
     * 绑定数据到 UI
     */
    fun bind(note: NotePreviewModel?) {
        this.currentNote = note

        // 如果 note 为 null（占位符），可以设置一些默认状态或骨架屏
        if (note == null) return

        titleTextView.text = note.title
        summaryTextView.text = note.summary

        // 根据排序方式决定显示哪个时间
        val currentOrder = getSortOrder()
        val timeToShow = when (currentOrder) {
            SortOrder.BY_CREATION_TIME_ASC,
            SortOrder.BY_CREATION_TIME_DESC -> note.createdTime

            SortOrder.BY_UPDATE_TIME_ASC,
            SortOrder.BY_UPDATE_TIME_DESC -> note.updatedTime
        }

        // 调用 DateUtils 的智能格式化
        timeTextView.text = DateUtils.getSmartDate(timeToShow)

        // 置顶图标
        pinnedImageView.visibility = if (note.isPinned) View.VISIBLE else View.GONE

        // 根据 UI Mode 处理复选框
        val mode = getUiMode()
        when (mode) {
            is HomeUiMode.Browsing -> {
                selectionCheckbox.visibility = View.GONE
                cardView.setCardBackgroundColor(defaultColor)
            }
            is HomeUiMode.Managing -> {
                val isSelected = note.noteId in mode.allSelectedIds
                selectionCheckbox.visibility = View.VISIBLE
                selectionCheckbox.isChecked = note.noteId in mode.allSelectedIds
                if (isSelected) {
                    cardView.setCardBackgroundColor(selectedColor)
                } else {
                    cardView.setCardBackgroundColor(defaultColor)
                }
            }
        }
    }

    companion object {
        /**
         * 创建 ViewHolder 的辅助方法
         */
        fun create(
            parent: ViewGroup,
            onItemClick: (NotePreviewModel) -> Unit,
            onItemLongClick: (NotePreviewModel) -> Boolean?,
            getUiMode: () -> HomeUiMode,
            getSortOrder: () -> SortOrder
        ): NotePreviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note_preview, parent, false)
            return NotePreviewViewHolder(view, onItemClick, onItemLongClick, getUiMode,getSortOrder)
        }
    }
}

/**
 * 【公共 DiffCallback】
 * 用于比较新旧数据，Paging 和 ListAdapter 都需要它。
 */
object NotePreviewDiffCallback : DiffUtil.ItemCallback<NotePreviewModel>() {
    override fun areItemsTheSame(oldItem: NotePreviewModel, newItem: NotePreviewModel): Boolean =
        oldItem.noteId == newItem.noteId

    override fun areContentsTheSame(oldItem: NotePreviewModel, newItem: NotePreviewModel): Boolean =
        oldItem == newItem
}