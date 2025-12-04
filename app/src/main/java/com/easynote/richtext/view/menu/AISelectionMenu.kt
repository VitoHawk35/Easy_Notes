package com.easynote.richtext.view.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.easynote.R
import com.easynote.ai.core.TaskType

class AISelectionMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    interface OnAIMenuActionListener {
        fun onAITask(taskType: TaskType)
        fun onBack() // 返回上一级
    }

    var actionListener: OnAIMenuActionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.ai_selection_menu, this, true)

        findViewById<TextView>(R.id.action_correct).setOnClickListener { actionListener?.onAITask(TaskType.CORRECT) }
        findViewById<TextView>(R.id.action_polish).setOnClickListener { actionListener?.onAITask(TaskType.POLISH) }
        findViewById<TextView>(R.id.action_translate).setOnClickListener { actionListener?.onAITask(TaskType.TRANSLATE) }
        findViewById<TextView>(R.id.action_summary).setOnClickListener { actionListener?.onAITask(TaskType.SUMMARY) }

        findViewById<TextView>(R.id.action_back).setOnClickListener { actionListener?.onBack() }
    }
}