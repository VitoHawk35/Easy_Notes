package com.easynote.home.ui.Adapter

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 【公共的笔记预览ViewHolder，供首页和日历页复用】
 * 独立的 ViewHolder，负责持有 View 和绑定数据逻辑。
 * 可以被 PagingAdapter 和 ListAdapter 复用。
 */
class NotePreviewViewHolder(
    itemView: View,
    private val onItemClick: (NotePreviewModel) -> Unit,
    private val onItemLongClick: (NotePreviewModel) -> Boolean,
    private val getUiMode: () -> HomeUiMode // 通过函数获取当前的 UI Mode，解耦
) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView: TextView = itemView.findViewById(R.id.textView_note_title)
    private val summaryTextView: TextView = itemView.findViewById(R.id.textView_note_summary)
    private val timeTextView: TextView = itemView.findViewById(R.id.textView_note_time)
    private val pinnedImageView: ImageView = itemView.findViewById(R.id.imageView_pinned)
    private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox_selection)

    // 当前绑定的数据项，用于点击事件
    private var currentNote: NotePreviewModel? = null

    init {
        // 单击事件
        itemView.setOnClickListener {
            currentNote?.let { note ->
                // 直接回调，让外部根据 UI Mode 处理
                onItemClick(note)
            }
        }

        // 长按事件
        itemView.setOnLongClickListener {
            currentNote?.let { note ->
                // 只在浏览模式下触发长按
                if (getUiMode() is HomeUiMode.Browsing) {
                    onItemLongClick(note)
                } else {
                    false
                }
            } ?: false
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

        // 格式化时间
        val date = Date(note.updatedTime)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        timeTextView.text = format.format(date)

        // 置顶图标
        pinnedImageView.visibility = if (note.isPinned) View.VISIBLE else View.GONE

        // 根据 UI Mode 处理复选框
        val mode = getUiMode()
        when (mode) {
            is HomeUiMode.Browsing -> {
                selectionCheckbox.visibility = View.GONE
            }
            is HomeUiMode.Managing -> {
                selectionCheckbox.visibility = View.VISIBLE
                selectionCheckbox.isChecked = note.noteId in mode.allSelectedIds
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
            onItemLongClick: (NotePreviewModel) -> Boolean,
            getUiMode: () -> HomeUiMode
        ): NotePreviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note_preview, parent, false)
            return NotePreviewViewHolder(view, onItemClick, onItemLongClick, getUiMode)
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