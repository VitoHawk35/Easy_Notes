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
import com.easynote.ai.core.TaskType

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
            calculateAndSetPosition(menuView, start, end)
        }
    }

    private fun calculateAndSetPosition(menuView: View, start: Int, end: Int) {
        val layout = etContent.layout ?: return

        // 1. 垂直方向逻辑
        val line = layout.getLineForOffset(start)
        val lineTop = layout.getLineTop(line)
        val lineBottom = layout.getLineBottom(line)

        val location = IntArray(2)
        etContent.getLocationOnScreen(location)
        val editTextScreenY = location[1]
        val editTextScreenX = location[0] // 获取 EditText 的左边缘 X

        // 预留间距 (px)
        val padding = 20

        // 垂直计算 (默认上方)
        var menuY = editTextScreenY + lineTop - menuView.height - padding

        // 垂直边界检测 (触顶反弹)
        val statusBarHeight = getStatusBarHeight()
        val minTopLimit = statusBarHeight + padding
        if (menuY < minTopLimit) {
            menuY = editTextScreenY + lineBottom + padding
        }

        // 2. 水平方向逻辑

        // 2.1 计算理想的中心点 X
        val startX = layout.getPrimaryHorizontal(start)
        val endX = layout.getPrimaryHorizontal(end)
        val selectionCenterX = (startX + endX) / 2

        // 2.2 计算理想的左上角 menuX
        var menuX = editTextScreenX + selectionCenterX + etContent.paddingLeft - (menuView.width / 2)

        // 2.3 获取屏幕宽度
        val displayMetrics = etContent.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // 2.4 左侧边界检测 (不能小于 padding)
        if (menuX < padding) {
            menuX = padding.toFloat()
        }

        // 2.5 右侧边界检测 (menuX + menuWidth 不能超过 screenWidth - padding)
        val maxMenuX = screenWidth - menuView.width - padding
        if (menuX > maxMenuX) {
            menuX = maxMenuX.toFloat()
        }

        // 3. 应用坐标
        menuView.x = menuX
        menuView.y = menuY.toFloat()

        if (menuView.visibility != View.VISIBLE) {
            menuView.visibility = View.VISIBLE
        }
    }

    // 辅助方法：获取状态栏高度
    private fun getStatusBarHeight(): Int {
        var result = 0
        val res = etContent.resources

        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
        return result
    }
}