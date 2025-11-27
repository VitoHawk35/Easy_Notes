package com.easynote.home.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mydemo.R
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.TagRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    // 使用自定义的 ViewModel Factory 来创建 HomeViewModel 实例
    private val viewModel: HomeViewModel by viewModels { //by viewModels的懒加载
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // 假设你的 Repository 是单例或者可以通过其他方式获取
                val noteRepository = NoteRepository(application)
                val tagRepository = TagRepository(application)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application, noteRepository, tagRepository) as T
            }
        }
    }

    // 笔记列表的 Adapter
    private val notePreviewAdapter = NotePreviewPagingAdapter()

    // 标签栏的控制器，设为 lateinit，在 onCreate 中初始化
    private lateinit var tagFilterRail: TagFilterRail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 设置布局文件
        // 确保你的 res/layout 目录下有一个名为 activity_home.xml 的布局文件
        setContentView(R.layout.activity_home_layout)

        // 2. 初始化标签栏 (TagFilterRail)
        val tagRecyclerView = findViewById<RecyclerView>(R.id.recyclerView_tagFilter)
        tagFilterRail = TagFilterRail(tagRecyclerView, viewModel, lifecycleScope)

        // 3. 初始化笔记列表
        setupNotesRecyclerView()

        // 4. 观察笔记数据流
        observeNotePreviews()
    }

    /**
     * 封装设置笔记列表 RecyclerView 的逻辑
     */
    private fun setupNotesRecyclerView() {
        val notesRecyclerView = findViewById<RecyclerView>(R.id.recyclerView_notePreviews)
        notesRecyclerView.adapter = notePreviewAdapter
        // 使用两列的瀑布流布局
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    /**
     * 封装观察 ViewModel 中笔记数据流的逻辑
     */
    private fun observeNotePreviews() {
        lifecycleScope.launch {
            // 使用 collectLatest，当有新的筛选条件时，会自动取消上一次的收集
            viewModel.notePreviews.collectLatest { pagingData ->
                // 将最新的分页数据提交给 Adapter
                notePreviewAdapter.submitData(pagingData)
            }
        }
    }
}
