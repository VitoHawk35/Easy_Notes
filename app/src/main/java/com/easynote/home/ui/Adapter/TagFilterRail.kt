package com.easynote.home.ui.Adapter

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easynote.home.ui.FilterAll
import com.easynote.home.ui.FilterByTags
import com.easynote.home.ui.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 一个辅助类，完全封装了标签筛选栏 RecyclerView 的所有逻辑。
 * 它负责创建、组合和管理内部的 Adapters。
 *
 * @param recyclerView 要被管理的 RecyclerView 实例。
 * @param viewModel HomeViewModel 实例，用于获取数据和发送事件。
 * @param lifecycleScope 用于启动协程来观察数据流的生命周期作用域。
 */
class TagFilterRail(
    private val recyclerView: RecyclerView,
    private val viewModel: HomeViewModel,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    // 1. 将 Adapters 设为私有成员，外部无需关心它们的具体实现
    private val allTagAdapter = AllTagHeaderAdapter {
        viewModel.onSelectAll()
    }
    private val tagPagingAdapter = TagPagingDataAdapter { clickedTag ->
        viewModel.onTagSelected(clickedTag)
    }

    // 2. 在初始化时就完成所有的设置工作
    init {
        val concatAdapter = ConcatAdapter(allTagAdapter, tagPagingAdapter)
        recyclerView.adapter = concatAdapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)

        observeData()
    }

    // 3. 将观察数据的逻辑也封装进来
    private fun observeData() {
        lifecycleScope.launch {
            viewModel.tags.collectLatest { pagingData ->
                tagPagingAdapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            viewModel.filterState.collect { state ->
                when (state) {
                    is FilterAll -> {
                        allTagAdapter.isSelected = true
                        tagPagingAdapter.selectedTagIds = emptySet()
                    }
                    is FilterByTags -> {
                        allTagAdapter.isSelected = false
                        tagPagingAdapter.selectedTagIds = state.selectedTagIds
                    }
                }
            }
        }
    }
}
