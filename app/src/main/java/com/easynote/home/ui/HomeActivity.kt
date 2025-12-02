package com.easynote.home.ui

import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.easynote.R
import com.easynote.data.repository.impl.NoteRepositoryImpl
import com.easynote.data.repository.impl.TagRepositoryImpl
import com.easynote.home.ui.fragmentimport.SettingsFragment
import com.easynote.databinding.ActivityHomeBinding
import com.easynote.databinding.DrawerHeaderDateFilterBinding
import com.easynote.home.ui.fragment.CalendarFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 应用的主 Activity，作为所有主页面 Fragment 的“宿主”或“外壳”。
 * 它的核心职责是管理底部导航栏和切换 Fragment。
 */
class HomeActivity : AppCompatActivity() {

    // 3. 使用 View Binding 来安全地访问 activity_home.xml 中的视图
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerBinding: DrawerHeaderDateFilterBinding
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            // 1. 获取由系统创建的、我们自定义的 Application 实例
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val noteRepository = NoteRepositoryImpl(application)
                val tagRepository = TagRepositoryImpl(application)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(
                    application,
                    noteRepository,
                    tagRepository
                ) as T
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        // 绑定侧边栏的视图
        drawerBinding = DrawerHeaderDateFilterBinding.bind(binding.navViewDrawer.getHeaderView(0))
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
            showManagementUI(mode is HomeUiMode.Managing)
        }.launchIn(lifecycleScope)
        //观察置顶按键ui
        viewModel.pinActionState.onEach { state ->
            updatePinActionItem(state)
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
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    viewModel.setCurrentScreen(Screen.Home) // 通知 ViewModel 当前是主页
                    true
                }
                R.id.nav_calendar -> {
                    replaceFragment(CalendarFragment()) // 假设你已经创建了 CalendarFragment
                    viewModel.setCurrentScreen(Screen.Calendar) // 通知 ViewModel 当前是日历页
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    viewModel.setCurrentScreen(Screen.Settings) // 通知 ViewModel 当前是设置页
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
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // 初始化 Spinner 的 Adapters
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, (currentYear - 10..currentYear).toList().reversed())
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..12).toList())
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        drawerBinding.spinnerStartYear.adapter = yearAdapter
        drawerBinding.spinnerEndYear.adapter = yearAdapter
        drawerBinding.spinnerStartMonth.adapter = monthAdapter
        drawerBinding.spinnerEndMonth.adapter = monthAdapter

        // 设置 Spinner 监听器
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDaySpinners()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        drawerBinding.spinnerStartYear.onItemSelectedListener = listener
        drawerBinding.spinnerStartMonth.onItemSelectedListener = listener
        drawerBinding.spinnerEndYear.onItemSelectedListener = listener
        drawerBinding.spinnerEndMonth.onItemSelectedListener = listener

        // 初始化默认日期
        drawerBinding.spinnerStartYear.setSelection(10) // 默认选中最早的年份
        drawerBinding.spinnerEndYear.setSelection(0)   // 默认选中当前年份
        drawerBinding.spinnerStartMonth.setSelection(0) // 1月
        drawerBinding.spinnerEndMonth.setSelection(currentMonth - 1)
        updateDaySpinners()
        drawerBinding.spinnerEndDay.setSelection(currentDay - 1)

        // 设置按钮点击事件
        drawerBinding.buttonApplyDateFilter.setOnClickListener {
                // 1. 从开始日期 Spinners 获取时间戳
                val startTimeStamp = getSelectedTimeStamp(
                    drawerBinding.spinnerStartYear,
                    drawerBinding.spinnerStartMonth,
                    drawerBinding.spinnerStartDay,
                    isStartOfDay = true // 设置为一天的开始
                )

                // 2. 从结束日期 Spinners 获取时间戳
                val endTimeStamp = getSelectedTimeStamp(
                    drawerBinding.spinnerEndYear,
                    drawerBinding.spinnerEndMonth,
                    drawerBinding.spinnerEndDay,
                    isStartOfDay = false // 设置为一天的结束
                )

                // 3. 调用 ViewModel 的方法
                viewModel.applyDateFilter(startTimeStamp, endTimeStamp)
            // TODO: 从 Spinner 获取选择的年月日，调用 viewModel.applyDateFilter()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        drawerBinding.buttonClearDateFilter.setOnClickListener {
            viewModel.clearDateFilter()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }
    /**
     * 【新增】一个辅助方法，用于从三个 Spinner 中获取选择的日期并转换为时间戳。
     * @param isStartOfDay 如果为 true，则将时间设置为 00:00:00；如果为 false，则设置为 23:59:59。
     * @return 返回计算好的时间戳（Long类型），如果任何一个 Spinner 没有选中项，则返回 null。
     */
    private fun getSelectedTimeStamp(
        yearSpinner: Spinner,
        monthSpinner: Spinner,
        daySpinner: Spinner,
        isStartOfDay: Boolean
    ): Long? {
        val year = yearSpinner.selectedItem as? Int
        val month = monthSpinner.selectedItem as? Int
        val day = daySpinner.selectedItem as? Int

        if (year != null && month != null && day != null) {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1) // Calendar 的月份是 0-based
                set(Calendar.DAY_OF_MONTH, day)
                if (isStartOfDay) {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                } else {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
            }.timeInMillis
        }
        return null
    }
    /**
     * 【新增】根据选中的年份和月份，动态更新“天”的 Spinner
     */
    private fun updateDaySpinners() {
        updateDaySpinner(drawerBinding.spinnerStartYear, drawerBinding.spinnerStartMonth, drawerBinding.spinnerStartDay)
        updateDaySpinner(drawerBinding.spinnerEndYear, drawerBinding.spinnerEndMonth, drawerBinding.spinnerEndDay)
        // TODO: 在这里添加结束日期不能早于开始日期的逻辑
    }

    private fun updateDaySpinner(yearSpinner: Spinner, monthSpinner: Spinner, daySpinner: Spinner) {
        val year = yearSpinner.selectedItem as Int
        val month = monthSpinner.selectedItem as Int
        val calendar = Calendar.getInstance().apply { set(year, month - 1, 1) }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..daysInMonth).toList())
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = dayAdapter
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
