package com.easynote.richtext.view.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.easynote.R

class SelectionToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    interface OnToolbarActionListener {
        fun onCopy()
        fun onCut()
        fun onPaste()
        fun onSelectAll() // 新增
        fun onAIEntry()   // 新增：点击 AI 按钮
    }

    var actionListener: OnToolbarActionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.selection_toolbar, this, true)

        findViewById<TextView>(R.id.action_copy).setOnClickListener { actionListener?.onCopy() }
        findViewById<TextView>(R.id.action_cut).setOnClickListener { actionListener?.onCut() }
        findViewById<TextView>(R.id.action_paste).setOnClickListener { actionListener?.onPaste() }
        findViewById<TextView>(R.id.action_select_all).setOnClickListener { actionListener?.onSelectAll() }
        findViewById<TextView>(R.id.action_ai_entry).setOnClickListener { actionListener?.onAIEntry() }
    }
}