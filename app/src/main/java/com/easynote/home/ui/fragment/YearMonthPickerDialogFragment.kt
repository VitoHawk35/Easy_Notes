package com.easynote.home.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources // 🟢 [新增] 用于安全获取 Drawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.easynote.R
import com.easynote.databinding.DialogYearMonthPickerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder // 🟢 [新增] 导入 Material Builder
import java.util.Calendar

class YearMonthPickerDialogFragment : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "YearMonthPickerDialogFragment.RequestKey"
        const val RESULT_KEY_YEAR = "Result.Year"
        const val RESULT_KEY_MONTH = "Result.Month"

        fun newInstance(year: Int, month: Int): YearMonthPickerDialogFragment {
            return YearMonthPickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(RESULT_KEY_YEAR, year)
                    putInt(RESULT_KEY_MONTH, month)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogYearMonthPickerBinding.inflate(requireActivity().layoutInflater)
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val initialYear = arguments?.getInt(RESULT_KEY_YEAR) ?: currentYear
        val initialMonth = arguments?.getInt(RESULT_KEY_MONTH) ?: (calendar.get(Calendar.MONTH) + 1)

        binding.pickerYear.apply {
            minValue = currentYear - 50
            maxValue = currentYear + 50
            value = initialYear
            wrapSelectorWheel = false
        }

        binding.pickerMonth.apply {
            minValue = 1
            maxValue = 12
            value = initialMonth
        }

        // 🟡 [修改] 使用 MaterialAlertDialogBuilder
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.Style_YearMonthPicker)
            .setTitle("选择年月")
            .setView(binding.root)
            // 🟢 [核心修改] 直接设置背景为你的圆角 Drawable
            .setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.bg_dialog_rounded))
            .setPositiveButton("确定") { _, _ ->
                val selectedYear = binding.pickerYear.value
                val selectedMonth = binding.pickerMonth.value

                setFragmentResult(REQUEST_KEY, Bundle().apply {
                    putInt(RESULT_KEY_YEAR, selectedYear)
                    putInt(RESULT_KEY_MONTH, selectedMonth)
                })
            }
            .setNegativeButton("取消", null)

        // 🟡 [修改] 直接 create 并返回即可，不需要再操作 window 了
        // MaterialBuilder 会自动处理好背景
        return builder.create()
    }
}