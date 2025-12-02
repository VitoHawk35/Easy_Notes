package com.easynote.home.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.easynote.databinding.DialogYearMonthPickerBinding
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

        return AlertDialog.Builder(requireContext())
            .setTitle("选择年月")
            .setView(binding.root)
            .setPositiveButton("确定") { _, _ ->
                val selectedYear = binding.pickerYear.value
                val selectedMonth = binding.pickerMonth.value

                // 【核心修改】使用 setFragmentResult，它会自动找到正确的 FragmentManager (在这里是 childFragmentManager)
                setFragmentResult(REQUEST_KEY, Bundle().apply {
                    putInt(RESULT_KEY_YEAR, selectedYear)
                    putInt(RESULT_KEY_MONTH, selectedMonth)
                })
            }
            .setNegativeButton("取消", null)
            .create()
    }
}
