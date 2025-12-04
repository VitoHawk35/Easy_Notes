package com.easynote.home.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easynote.R
import com.easynote.data.repository.impl.RepositoryImpl
import com.easynote.databinding.ActivityTagManagementBinding
import com.easynote.home.domain.model.TagModel
// 🟢 [新增] 引入独立的 Adapter 类
import com.easynote.home.ui.Adapter.TagListAdapter
import com.easynote.home.ui.fragment.TagDialogFragment
import com.easynote.home.ui.viewmodel.TagManagementViewModel
import com.easynote.home.ui.viewmodel.TagUiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TagManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManagementBinding

    private val viewModel: TagManagementViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = RepositoryImpl(application)
                @Suppress("UNCHECKED_CAST")
                return TagManagementViewModel(application, repo) as T
            }
        }
    }

    // Adapter 定义 (无需修改实例化逻辑，只要 import 对了即可)
    private val adapter = TagListAdapter(
        onEditClick = { tag -> showAddEditDialog(tag) },
        onDeleteClick = { tag -> viewModel.deleteTag(tag) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // RecyclerView
        binding.recyclerViewTags.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTags.adapter = adapter

        // 数据观察
        lifecycleScope.launch {
            viewModel.tags.collectLatest { tags ->
                adapter.submitList(tags)
            }
        }

        // 事件观察
        lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is TagUiEvent.ShowToast -> {
                        Toast.makeText(this@TagManagementActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tag_management_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_add_tag -> {
                showAddEditDialog(null) // 新建
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddEditDialog(tag: TagModel?) {
        val dialog = TagDialogFragment.newInstance(tag)
        dialog.onConfirmListener = { id, name, color ->
            if (id == null) {
                viewModel.insertTag(name, color)
            } else {
                viewModel.updateTag(id, name, color)
            }
        }
        dialog.show(supportFragmentManager, "TagDialog")
    }
}
