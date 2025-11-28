package com.easynote.home.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com..easynote.R
import com.easynote.data.repository.NoteRepository
import com.easynote.data.repository.TagRepository
import com.easynote.home.ui.fragmentimport.SettingsFragment
import com.easynote.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

/**
 * 应用的主 Activity，作为所有主页面 Fragment 的“宿主”或“外壳”。
 * 它的核心职责是管理底部导航栏和切换 Fragment。
 */
class HomeActivity : AppCompatActivity() {

    // 3. 使用 View Binding 来安全地访问 activity_home.xml 中的视图
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val noteRepository = NoteRepository(application)
                val tagRepository = TagRepository(application)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application, noteRepository, tagRepository) as T
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //设置 Toolbar 作为应用的 ActionBar
        setSupportActionBar(binding.toolbar)
        //在 Activity 首次创建时，默认加载 HomeFragment
        if (savedInstanceState == null) {
            // 我们将 HomeFragment 设置为默认显示的页面
            replaceFragment(HomeFragment())
        }
        //底部导航栏的Home默认选中
        binding.bottomNavViewBrowse.selectedItemId = R.id.nav_home
        // 设置预览模式下底部导航栏的点击事件监听器
        setupBrowseBottomNavigation()
        // 设置管理模式下底部导航栏的点击事件监听器
        setupManagementNavigation()
        //观察 UI 模式以更新顶部菜单
        observeUiMode()
    }

    // 根据uimode加载顶部菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 根据当前模式加载不同的菜单文件
        when (viewModel.uiMode.value) {
            is HomeUiMode.Browsing -> menuInflater.inflate(R.menu.home_browse_top_menu, menu)
            is HomeUiMode.Managing -> menuInflater.inflate(R.menu.home_management_top_menu, menu)
        }
        return true
    }
    // onOptionsItemSelected 来处理菜单项点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 处理管理模式下的“全选”按钮点击
            R.id.action_select_all -> {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                if (currentFragment is HomeFragment) {
                    currentFragment.onSelectAllActionClicked()
                }
                true
            }
            // 处理“退出管理模式”的叉号按钮点击
            android.R.id.home -> {
                if (viewModel.uiMode.value is HomeUiMode.Managing) {
                    viewModel.exitManagementMode()
                    true
                } else {
                    // 如果不是管理模式，则执行默认的返回操作
                    super.onOptionsItemSelected(item)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    /**
     * 观察 ViewModel 的 UI 模式变化，并据此更新 UI。
     */
    private fun observeUiMode() {
        lifecycleScope.launch {
            viewModel.uiMode.collect { mode ->
                showManagementUI(mode is HomeUiMode.Managing)
            }
        }
    }

    /**
     * 公开方法，供 Fragment 调用，用于切换底部和顶部 UI 的显示。
     * @param show true 表示显示管理模式UI；false 则显示浏览模式UI。
     */
    fun showManagementUI(show: Boolean) {
        if (show) {
            // 进入管理模式
            binding.bottomNavViewBrowse.visibility = View.GONE
            binding.bottomNavViewManage.visibility = View.VISIBLE
            // 【新增】显示返回的叉号图标，并设置标题
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close) // 你需要一个关闭图标
            supportActionBar?.title = "已选择 ${ (viewModel.uiMode.value as? HomeUiMode.Managing)?.selectedNoteIds?.size ?: 0 } 项"

        } else {
            // 退出管理模式
            binding.bottomNavViewBrowse.visibility = View.VISIBLE
            binding.bottomNavViewManage.visibility = View.GONE
            // 【新增】隐藏返回图标，恢复默认标题
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.title = getString(R.string.app_name) // 恢复应用名作为标题
        }
        // 【新增】通知系统重新创建菜单
        invalidateOptionsMenu()
    }
    /**
     * 浏览模式下底部导航栏的按钮设置点击事件。
     */
    private fun setupBrowseBottomNavigation() {
        binding.bottomNavViewBrowse.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true // 返回 true 表示事件已处理
                }
                R.id.nav_calendar -> {
                    // replaceFragment(CalendarFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false // 如果是未知的ID，返回 false
            }
        }
    }

    /**
     * 设置管理模式导航栏的点击事件，调用HomeFragment暴露的接口。
     */
    private fun setupManagementNavigation() {
        binding.bottomNavViewManage.setOnItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment is HomeFragment) {
                // 根据点击的按钮ID，调用 Fragment 对应的公开方法
                when (menuItem.itemId) {
                    R.id.action_pin -> currentFragment.onPinActionClicked()
                    R.id.action_delete -> currentFragment.onDeleteActionClicked()
                }
            }
            // 无论 Fragment 是否处理，都返回 true，因为我们不希望有任何默认的选中效果
            true
        }
    }


    /**
     * 一个通用的辅助方法，用于在 fragment_container_view 中替换 Fragment。
     * @param fragment 要显示的 Fragment 实例。
     */
    private fun replaceFragment(fragment: Fragment) {
        // 获取 FragmentManager 并开始一个事务
        supportFragmentManager.beginTransaction()
            // 将指定的 Fragment 替换到 ID 为 fragment_container_view 的容器中
            .replace(R.id.fragment_container_view, fragment)
            // 提交事务以使更改生效
            .commit()
    }
}
