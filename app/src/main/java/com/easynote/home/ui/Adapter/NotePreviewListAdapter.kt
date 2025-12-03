package com.easynote.home.ui.Adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter // 注意导入的是 ListAdapter
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.ui.HomeUiMode

/**
 * 普通 List 版本的 Adapter (用于日历页的全量数据)
 * 继承自 ListAdapter，它内部已经处理了 List 的 Diff 逻辑。
 */
class NotePreviewListAdapter(
    private val onItemClick: (NotePreviewModel) -> Unit,
    private val onItemLongClick: (NotePreviewModel) -> Boolean
) : ListAdapter<NotePreviewModel, NotePreviewViewHolder>(NotePreviewDiffCallback) {

    // 日历页通常只在 Browsing 模式，但为了兼容性还是留着
    var currentUiMode: HomeUiMode = HomeUiMode.Browsing
        set(value) {
            if (field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePreviewViewHolder {
        // 同样复用公共 ViewHolder
        return NotePreviewViewHolder.create(
            parent,
            onItemClick,
            onItemLongClick,
            getUiMode = { currentUiMode }
        )
    }

    override fun onBindViewHolder(holder: NotePreviewViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
}