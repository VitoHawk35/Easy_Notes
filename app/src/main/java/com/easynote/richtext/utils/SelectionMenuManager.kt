package com.easynote.richtext.utils

import android.R
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.easynote.richtext.view.menu.AISelectionMenu
import com.easynote.richtext.view.menu.SelectionToolbar
import com.example.mydemo.ai.core.TaskType

/**
 * 负责管理文本选区的悬浮菜单 (UI 逻辑)
 */
class SelectionMenuManager(
    private val container: ViewGroup, // 传入 RichTextView
    private val etContent: EditText
) {
    private var selectionToolbar: SelectionToolbar? = null
    private var aiSelectionMenu: AISelectionMenu? = null

    // 对外回调
    var onAITaskTriggered: ((TaskType) -> Unit)? = null

    init {
        setupFloatingMenus()
        setupSystemSelectionOverride()
    }

    private fun setupFloatingMenus() {
        val context = container.context

        // 1. 一级菜单
        selectionToolbar = SelectionToolbar(context).apply {
            visibility = View.GONE
            actionListener = object : SelectionToolbar.OnToolbarActionListener {
                override fun onCopy() { etContent.onTextContextMenuItem(R.id.copy); hideAllMenus() }
                override fun onCut() { etContent.onTextContextMenuItem(R.id.cut); hideAllMenus() }
                override fun onPaste() { etContent.onTextContextMenuItem(R.id.paste); hideAllMenus() }
                override fun onSelectAll() { etContent.selectAll() }
                override fun onAIEntry() {
                    selectionToolbar?.visibility = View.GONE
                    showMenuAtSelection(aiSelectionMenu)
                }
            }
        }
        container.addView(selectionToolbar, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // 2. 二级菜单
        aiSelectionMenu = AISelectionMenu(context).apply {
            visibility = View.GONE
            actionListener = object : AISelectionMenu.OnAIMenuActionListener {
                override fun onAITask(taskType: TaskType) {
                    hideAllMenus()
                    onAITaskTriggered?.invoke(taskType)
                }
                override fun onBack() {
                    aiSelectionMenu?.visibility = View.GONE
                    showMenuAtSelection(selectionToolbar)
                }
            }
        }
        container.addView(aiSelectionMenu, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun setupSystemSelectionOverride() {
        etContent.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = true
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()
                showMenuAtSelection(selectionToolbar)
                return false
            }
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false
            override fun onDestroyActionMode(mode: ActionMode?) {
                hideAllMenus()
            }
        }
    }

    fun hideAllMenus() {
        selectionToolbar?.visibility = View.GONE
        aiSelectionMenu?.visibility = View.GONE
    }

    // 复用之前的坐标计算逻辑，保持不变
    private fun showMenuAtSelection(menuView: View?) {
        if (menuView == null) return
        val start = etContent.selectionStart
        val end = etContent.selectionEnd
        if (start < 0 || end < 0 || start == end) return

        if (menuView == selectionToolbar) aiSelectionMenu?.visibility = View.GONE
        if (menuView == aiSelectionMenu) selectionToolbar?.visibility = View.GONE

        menuView.visibility = View.VISIBLE
        menuView.post {
            // ... (此处粘贴之前优化过的 calculatePosition 逻辑) ...
            // 为了篇幅简洁，这里省略具体数学计算代码，直接复用你现在的逻辑
            calculateAndSetPosition(menuView, start, end)
        }
    }

    private fun calculateAndSetPosition(menuView: View, start: Int, end: Int) {
        // ... 把 RichTextView 里那个复杂的 post 代码块搬过来 ...
        // 注意：原代码中的 `this.width` 需改为 `container.width`
        // 原代码中的 `this.getLocationInWindow` 需改为 `container.getLocationInWindow`
        menuView.post {
            val layout = etContent.layout ?: return@post
            val toolbarWidth = menuView.width
            val toolbarHeight = menuView.height
            val parentWidth = container.width

            // 计算选区中心
            val startX = layout.getPrimaryHorizontal(start)
            val endX = layout.getPrimaryHorizontal(end)
            val centerX = (startX + endX) / 2

            val coords = IntArray(2)
            etContent.getLocationInWindow(coords)
            val parentCoords = IntArray(2)
            container.getLocationInWindow(parentCoords)

            val rawX = (coords[0] + centerX) - parentCoords[0] - (toolbarWidth / 2)

            // 安全边界逻辑
            val margin = 20f
            val maxRight = (parentWidth - toolbarWidth).toFloat() - margin
            val safeMaxX = kotlin.math.max(margin, maxRight)
            val finalX = rawX.coerceIn(margin, safeMaxX)

            // 上下翻转逻辑
            val line = layout.getLineForOffset(start)
            val lineTop = layout.getLineTop(line)
            val lineBottom = layout.getLineBottom(line)

            var finalY = (coords[1] + lineTop) - parentCoords[1] - toolbarHeight - 20f
            if (finalY < 0) {
                finalY = (coords[1] + lineBottom) - parentCoords[1] + 10f
            }

            menuView.x = finalX
            menuView.y = finalY
        }
    }
}