package com.easynote.home.ui.fragmentimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.easynote.R
import com.easynote.data.repository.impl.NoteRepositoryImpl
import com.easynote.data.repository.impl.TagRepositoryImpl
import com.easynote.databinding.FragmentSettingsBinding // 【重要】导入 ViewBinding 类
import com.easynote.home.ui.HomeViewModel
import com.easynote.home.ui.LayoutMode
import com.easynote.home.ui.SortOrder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import android.util.Log
/**
 * 设置页面 Fragment，使用 Spinner 实现下拉菜单来修改应用的排序和布局方式。
 */
class SettingsFragment : Fragment() {

    // 1. 使用 Fragment 的 View Binding 标准模式
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // 2. 【核心】通过 activityViewModels 获取与 Activity 绑定的同一个 HomeViewModel 实例
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. 初始化 Spinners 的 Adapters
        setupSpinners()

        // 4. 观察 ViewModel 的状态以更新UI
        observeSettings()

        // 5. 为 UI 控件设置监听器以更新 ViewModel
        setupListeners()
    }

    /**
     * 为 Spinners 设置 Adapters，从 arrays.xml 加载数据。
     */
    private fun setupSpinners() {
        // 为排序 Spinner 设置 Adapter
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_order_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSortOrder.adapter = adapter
        }

        // 为布局 Spinner 设置 Adapter
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.layout_mode_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLayoutMode.adapter = adapter
        }
    }

    /**
     * 观察 ViewModel 中的设置状态，并据此更新 Spinner 的选中项。
     * 这是 "ViewModel -> UI" 的数据流。
     */
    private fun observeSettings() {
        // 观察排序方式
        viewModel.sortOrder.onEach { sortOrder ->
            val position = when (sortOrder) {
                SortOrder.BY_UPDATE_TIME_ASC -> 0
                SortOrder.BY_UPDATE_TIME_DESC -> 1
                SortOrder.BY_CREATION_TIME_ASC -> 2
                SortOrder.BY_CREATION_TIME_DESC -> 3
            }
            // 防止重复设置触发监听器
            if (binding.spinnerSortOrder.selectedItemPosition != position) {
                binding.spinnerSortOrder.setSelection(position)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        // 观察布局方式
        viewModel.layoutMode.onEach { layoutMode ->
            val position = when (layoutMode) {
                LayoutMode.GRID -> 0
                LayoutMode.LIST -> 1
            }
            if (binding.spinnerLayoutMode.selectedItemPosition != position) {
                binding.spinnerLayoutMode.setSelection(position)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    /**
     * 为 Spinner 控件设置监听器，当用户交互时，调用 ViewModel 的方法来更新状态。
     * 这是 "UI -> ViewModel" 的事件流。
     */
    private fun setupListeners() {
        binding.spinnerSortOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newSortOrder = when (position) {
                    0-> SortOrder.BY_UPDATE_TIME_ASC
                    1-> SortOrder.BY_UPDATE_TIME_DESC
                    2 -> SortOrder.BY_CREATION_TIME_ASC
                    3 -> SortOrder.BY_CREATION_TIME_DESC
                    else -> {SortOrder.BY_UPDATE_TIME_DESC}
                }
                // 只有在新选择的值和 ViewModel 中当前的值不同时才更新，防止重复调用
                if (viewModel.sortOrder.value != newSortOrder) {
                    viewModel.setSortOrder(newSortOrder)
                    //更改排序记录下log
                    Log.d("SettingsFragment", "设置排序方式Sort order changed to: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 在这个场景下通常无需处理
            }
        }

        binding.spinnerLayoutMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newLayoutMode = when (position) {
                    1 -> LayoutMode.LIST
                    else -> LayoutMode.GRID
                }
                if (viewModel.layoutMode.value != newLayoutMode) {
                    viewModel.setLayoutMode(newLayoutMode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 在这个场景下通常无需处理
            }
        }
    }

    /**
     * 当 Fragment 的视图被销毁时，清理 _binding 以防止内存泄漏。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
