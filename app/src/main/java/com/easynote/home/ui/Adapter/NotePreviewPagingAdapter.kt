package com.easynote.home.ui.Adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.easynote.home.domain.model.NotePreviewModel
import com.easynote.home.ui.HomeUiMode
import com.easynote.home.ui.SortOrder

/**
 * Paging 版本的 Adapter (用于首页无限流)
 */
class NotePreviewPagingAdapter(
    private val onItemClick: (NotePreviewModel) -> Unit,
    private val onItemLongClick: (NotePreviewModel) -> Boolean
) : PagingDataAdapter<NotePreviewModel, NotePreviewViewHolder>(NotePreviewDiffCallback) {


    // 当前 UI 模式
    var currentUiMode: HomeUiMode = HomeUiMode.Browsing
        set(value) {
            if (field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }
    var currentSortOrder: SortOrder = SortOrder.BY_CREATION_TIME_DESC
        set(value) {
            if (field != value) {
                field = value
                // 排序变了，需要刷新列表以更新时间显示
                notifyItemRangeChanged(0, itemCount)
            }
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePreviewViewHolder {
        // 复用公共 ViewHolder 的创建逻辑
        return NotePreviewViewHolder.create(
            parent,
            onItemClick,
            onItemLongClick,
            getUiMode = { currentUiMode } ,
            getSortOrder = { currentSortOrder }
        )
    }

    override fun onBindViewHolder(holder: NotePreviewViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
}