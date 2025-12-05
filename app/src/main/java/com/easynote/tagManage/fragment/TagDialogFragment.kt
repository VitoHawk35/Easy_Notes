package com.easynote.home.ui.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.easynote.R
import com.easynote.databinding.DialogAddEditTagBinding
import com.easynote.home.domain.model.TagModel

class TagDialogFragment : DialogFragment() {

    private var _binding: DialogAddEditTagBinding? = null
    private val binding get() = _binding!!

    // 回调函数：(TagId?, Name, Color) -> Unit
    // tagId 为 null 表示新增，否则为修改
    var onConfirmListener: ((Long?, String, String) -> Unit)? = null

    // 预设颜色列表
    private val colorList = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#FFC107",
        "#FF9800", "#FF5722", "#795548", "#9E9E9E"
    )
    private var currentColorIndex = 0
    private var editingTag: TagModel? = null // 如果是修改，这里有值

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddEditTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 获取传递过来的 Tag 数据 (如果是修改模式)
        arguments?.getParcelable<TagModel>("TAG_DATA")?.let {
            editingTag = it
        }

        setupUI()
    }

    private fun setupUI() {
        // 设置标题和默认值
        if (editingTag != null) {
            binding.tvDialogTitle.text = "修改标签"
            binding.etTagName.setText(editingTag!!.tagName)
            // 找到当前颜色的索引，如果没有则默认 0
            currentColorIndex = colorList.indexOf(editingTag!!.color).takeIf { it >= 0 } ?: 0
        } else {
            binding.tvDialogTitle.text = "新建标签"
            binding.etTagName.setText("新建标签")
            currentColorIndex = 0 // 默认红色
        }

        updateColorView()
        validateInput() // 初始化确认按钮状态

        // 颜色点击切换
        binding.viewColorPicker.setOnClickListener {
            currentColorIndex = (currentColorIndex + 1) % colorList.size
            updateColorView()
        }

        // 文本监听
        binding.etTagName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validateInput()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 按钮事件
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnConfirm.setOnClickListener {
            val name = binding.etTagName.text.toString().trim()
            val color = colorList[currentColorIndex]
            val id = editingTag?.tagId // null 表示新增

            onConfirmListener?.invoke(id, name, color)
            dismiss()
        }
    }

    private fun updateColorView() {
        val colorHex = colorList[currentColorIndex]
        val drawable = binding.viewColorPicker.background as GradientDrawable
        drawable.setColor(Color.parseColor(colorHex))
    }

    private fun validateInput() {
        val text = binding.etTagName.text.toString().trim()
        binding.btnConfirm.isEnabled = text.isNotEmpty()
        binding.btnConfirm.alpha = if (text.isNotEmpty()) 1.0f else 0.5f
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded)
        // 设置弹窗宽度
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(tag: TagModel? = null): TagDialogFragment {
            val fragment = TagDialogFragment()
            val args = Bundle()
            if (tag != null) {
                args.putParcelable("TAG_DATA", tag)
            }
            fragment.arguments = args
            return fragment
        }
    }
}