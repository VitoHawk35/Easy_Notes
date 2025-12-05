package com.easynote.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.easynote.R
import com.easynote.data.repository.impl.RepositoryImpl
import androidx.activity.OnBackPressedCallback
import com.easynote.home.ui.fragmentimport.SettingsFragment
import com.easynote.databinding.ActivityHomeBinding
import com.easynote.databinding.DrawerHeaderDateFilterBinding
import com.easynote.home.ui.fragment.CalendarFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
/**
 * 应用的主 Activity，作为所有主页面 Fragment 的“宿主”或“外壳”。
 * 它的核心职责是管理底部导航栏和切换 Fragment。
 */
class HomeActivity : AppCompatActivity() {

    // 3. 使用 View Binding 来安全地访问 activity_home.xml 中的视图
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerBinding: DrawerHeaderDateFilterBinding

    // 侧边栏逻辑控制器
    private lateinit var dateFilterHelper: DateFilterViewHelper
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            // 1. 获取由系统创建的、我们自定义的 Application 实例
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = RepositoryImpl(application)

                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application, repository) as T
            }
        }
    }
    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            // 当拦截生效时执行：退出管理模式
            viewModel.exitManagementMode()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        // 绑定侧边栏的视图
        drawerBinding = DrawerHeaderDateFilterBinding.bind(binding.navViewDrawer.getHeaderView(0))
        setContentView(binding.root)
        //  解决状态栏遮挡 Toolbar 问题
        // 这样只让主内容区域避开状态栏，Toolbar 就会正确显示在状态栏下方
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 设置 Padding：
            // top: 避开状态栏 (解决顶部菜单不可点击问题)
            // bottom: 避开底部手势条 (解决底部按钮被遮挡问题)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }
        // 🟢 [新增] 2. 单独处理侧边栏 (Drawer) 的 Edge-to-Edge
        // 这样“按日期筛选”几个字就会被 Padding 顶下来，不会和状态栏重叠
        ViewCompat.setOnApplyWindowInsetsListener(drawerBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 只需要设置 Top Padding (状态栏高度)
            // 左右下保持原样 (或者也加上 bottom 以避开手势条)
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)

            insets
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        //设置 Toolbar 作为应用的 ActionBar
        setSupportActionBar(binding.toolbar)
        //在 Activity 首次创建时，默认加载 HomeFragment
        if (savedInstanceState == null) {
            // 我们将 HomeFragment 设置为默认显示的页面
            replaceFragment(HomeFragment())
        }
        //底部导航栏的Home默认选中
        binding.bottomNavViewBrowse.selectedItemId = R.id.nav_home
        setupDrawer()//侧边菜单栏
        // 设置预览模式下底部导航栏的点击事件监听器
        setupBrowseBottomNavigation()
        // 设置管理模式下底部导航栏的点击事件监听器
        setupBottomManageNavigation()
        //观察 UI 模式以更新顶部底部菜单
        observeViewModelStates()
    }

    // 根据uimode加载顶部菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 使用 BottomNav 的选中项来判断当前页面，这比 findFragmentById 更稳定
        val currentItemId = binding.bottomNavViewBrowse.selectedItemId

        // 如果不在主页 (即在 日历 或 设置)，不加载任何菜单图标
        if (currentItemId != R.id.nav_home) {
            return false
        }

        // 只有在主页时，才根据模式加载菜单
        when (viewModel.uiMode.value) {
            is HomeUiMode.Browsing -> menuInflater.inflate(R.menu.home_browse_top_menu, menu)
            is HomeUiMode.Managing -> menuInflater.inflate(R.menu.home_management_top_menu, menu)
        }
        return true
    }

    // onOptionsItemSelected 来处理菜单项点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 跳转到标签管理页面
            R.id.action_manage_tags -> {
                startActivity(Intent(this, TagManagementActivity::class.java))
                true
            }
            R.id.action_menu -> {
                binding.drawerLayout.openDrawer(GravityCompat.END)
                true // 返回 true 表示事件已处理
            }
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
    private fun observeViewModelStates() {
        //观察更新浏览/管理模式ui
        viewModel.uiMode.onEach { mode ->
            val isManaging = mode is HomeUiMode.Managing
            showManagementUI(mode is HomeUiMode.Managing)
            // 如果是管理模式 -> isEnabled = true (拦截返回键，执行 exitManagementMode)
            // 如果是浏览模式 -> isEnabled = false (不拦截，执行系统默认返回，即退出 App)
            backPressedCallback.isEnabled = isManaging
            if (isManaging) {
                val managingState = mode as HomeUiMode.Managing
                updateBottomManageButtons(!managingState.isSelectionEmpty)
            }
        }.launchIn(lifecycleScope)
        //观察置顶按键ui
        viewModel.pinActionState.onEach { state ->
            updatePinActionItem(state)
            val isManaging = viewModel.uiMode.value as? HomeUiMode.Managing
            if (isManaging != null) {
                updateBottomManageButtons(!isManaging.isSelectionEmpty)
            }
        }.launchIn(lifecycleScope)
    }
    /**
     * 更新“置顶/取消置顶”菜单项 UI 的方法。
     */
    private fun updatePinActionItem(state: PinActionState) {
        // 1. 获取底部管理菜单中我们合并后的那个 item
        val pinMenuItem = binding.bottomNavViewManage.menu.findItem(R.id.action_toggle_pin)

        // 2. 安全地更新它的图标和标题
        pinMenuItem?.let { item ->
            when (state) {
                PinActionState.PIN -> {
                    item.title = getString(R.string.action_pin)
                    item.setIcon(R.drawable.ic_pinned) // “置顶”图标
                }
                PinActionState.UNPIN -> {
                    item.title = getString(R.string.action_unpin)
                    item.setIcon(R.drawable.ic_unpin)   // 假设 ic_unpin 是“取消置顶”图标
                }
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
            supportActionBar?.title = "已选择 ${ (viewModel.uiMode.value as? HomeUiMode.Managing)?.allSelectedIds?.size ?: 0 } 项"

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
            if (binding.bottomNavViewBrowse.selectedItemId == menuItem.itemId) {
                return@setOnItemSelectedListener false
            }
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    // 1. 设置标题
                    supportActionBar?.title = getString(R.string.app_name)
                    // 2. 刷新菜单 (会触发 onCreateOptionsMenu，加载 Home 菜单)
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_calendar -> {
                    replaceFragment(CalendarFragment()) // 假设你已经创建了 CalendarFragment
                    supportActionBar?.title = "日历"
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    supportActionBar?.title = "设置"
                    invalidateOptionsMenu()
                    true
                }
                else -> false
            }
            
        }
    }

    /**
     * 设置管理模式底部导航栏的点击事件，调用HomeFragment暴露的接口。
     */
    private fun setupBottomManageNavigation() {
        binding.bottomNavViewManage.setOnItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment is HomeFragment) {
                // 根据点击的按钮ID，调用 Fragment 对应的公开方法
                when (menuItem.itemId) {
                    R.id.action_toggle_pin -> currentFragment.onPinActionClicked()
                    R.id.action_delete -> currentFragment.onDeleteActionClicked()
                }
            }
            // 无论 Fragment 是否处理，都返回 true，因为我们不希望有任何默认的选中效果
            true
        }
    }
    /**
     * 【新增】设置侧边栏中所有 Spinner 和按钮的逻辑
     */
    private fun setupDrawer() {
        // 1. 实例化 Helper，只负责 UI 初始化
        dateFilterHelper = DateFilterViewHelper(this, drawerBinding)
        dateFilterHelper.setup()

        // 2. 设置“应用”按钮的点击事件 (逻辑写在 Activity 里)
        drawerBinding.buttonApplyDateFilter.setOnClickListener {

            // 从 Helper 获取选中的时间
            val range = dateFilterHelper.getSelectedDateRange()

            if (range != null) {
                val (start, end) = range

                // 验证逻辑：结束时间不得早于开始时间
                if (end < start) {
                    Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show()
                    // 阻断操作，不关闭侧边栏，不更新 ViewModel
                    return@setOnClickListener
                }

                // 验证通过，更新 ViewModel
                viewModel.applyDateFilter(start, end)
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        // 3. 设置“重置”按钮
        drawerBinding.buttonClearDateFilter.setOnClickListener {
            viewModel.clearDateFilter()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
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
    /**
     * 🟢 [新增] 控制底部管理菜单按钮的可用性和视觉状态
     * @param enable true 表示有选中项（可用），false 表示无选中项（置灰）
     */
    private fun updateBottomManageButtons(enable: Boolean) {
        val menu = binding.bottomNavViewManage.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            // 这一句代码就会触发 XML 中的 android:state_enabled 选择器
            // 自动切换 图标 和 文字 的颜色
            item.isEnabled = enable
        }
    }
}
