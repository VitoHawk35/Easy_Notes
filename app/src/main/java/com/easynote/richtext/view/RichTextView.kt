package com.easynote.richtext.view

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.easynote.R
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView

import com.easynote.richtext.view.impl.RichTextController
import com.easynote.richtext.utils.SelectionMenuManager
import com.easynote.ai.core.TaskType
import kotlinx.coroutines.Job
import android.app.Activity
/**
 * 富文本编辑器组件
 * * 使用说明：
 * 1. 在 XML 中引入此 View
 * 2. 设置监听器：[setOnRichTextListener]
 * 3. 设置内容：[setHtml] 或 [html] 属性
 * 4. 插入图片：[insertImage]
 */
class RichTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // UI 组件
    private var etContent: EditText
    private var btnBold: ImageView
    private var btnItalic: ImageView
    private var btnCancel: ImageView
    private var btnRecover: ImageView
    private var btnAddPicture: ImageView
    private var btnSave: ImageView

    // AI处理
    private lateinit var aiLoadingCard: CardView
    private lateinit var btnStopAi: View
    private var aiJob: Job? = null // 用于存储协程任务，方便取消

    // 核心逻辑控制器 (延迟初始化)
    private var controller: RichTextController? = null

    // 缓存初始数据（防止 Controller 还没初始化时外部就调用了 setHtml）
    private var pendingHtml: String? = null

    private var menuManager: SelectionMenuManager? = null

    // 对外暴露的监听接口
    interface OnRichTextListener {
        fun onSave(html: String)           // 点击保存按钮
        fun onInsertImageRequest()         // 点击插入图片按钮
        fun onContentChanged(html: String) // 内容实时变更（可选，用于自动保存）

        // callback 是外界处理完后调用的，传入处理后的文本
        fun onAIRequest(text: String, taskType: TaskType, context: String?, onResult: (String) -> Unit)
        //更新摘要的回调
        fun onUpdateAbstract(abstract: String)
    }

    private var listener: OnRichTextListener? = null

    // ================== 对外 API ==================

    /**
     * 设置监听器
     */
    fun setOnRichTextListener(listener: OnRichTextListener) {
        this.listener = listener
    }

    /**
     * 获取或设置 HTML 内容
     */
    var html: String
        get() = controller?.exportHtml() ?: ""
        set(value) {
            if (controller != null) {
                // 必须在 View 布局完成后（有宽度）才能加载 HTML
                post { controller?.loadHtml(value) }
            } else {
                pendingHtml = value // 存起来，等初始化好了再加载
            }
        }

    /**
     * 插入图片
     * @param uri 图片的 Uri
     */
    fun insertImage(uri: Uri) {


        controller?.insertImage(uri)
    }

    /**
     * 设置只读模式
     */
    fun setReadOnly(readOnly: Boolean) {
        controller?.isReadOnlyMode = readOnly
        updateUIForReadOnly(readOnly)
    }

    // ================== 内部逻辑 ==================

    init {
        LayoutInflater.from(context).inflate(R.layout.rich_text, this, true)

        // 绑定 View
        etContent = findViewById(R.id.et_content)
        btnBold = findViewById(R.id.btn_bold)
        btnItalic = findViewById(R.id.btn_italic)
        btnCancel = findViewById(R.id.btn_cancel)
        btnRecover = findViewById(R.id.btn_recover)
        btnAddPicture = findViewById(R.id.btn_add_image)
        btnSave = findViewById(R.id.btn_save)
        aiLoadingCard = findViewById(R.id.cv_ai_loading)
        btnStopAi = findViewById(R.id.btn_stop_ai)
        setupWindowInsets()
        setupClickListeners()
        initMenuManager()
    }

    // 【新】初始化菜单管理器
    private fun initMenuManager() {
        menuManager = SelectionMenuManager(this, etContent)

        // 绑定 AI 任务触发事件
        menuManager?.onAITaskTriggered = { taskType ->
            showAiLoading()

            when(taskType){
                TaskType.TRANSLATE -> {
                    val start = etContent.selectionStart
                    val end = etContent.selectionEnd
                    if (start < end) {
                        val text = etContent.text.substring(start, end)
                        // 获取全文作为上下文
                        val contextText = etContent.text.toString()

                        // 传入 context
                        listener?.onAIRequest(text, taskType, contextText) { resultText ->
                            hideAiLoading()
                            showAIResultDialog(taskType, resultText, start, end)
                        }
                    } else {
                        hideAiLoading()
                        Toast.makeText(context, "请先选择需要翻译的文本", Toast.LENGTH_SHORT).show()
                    }
                }
                TaskType.SUMMARY -> {
                    val text = etContent.text.toString()
                    // 委托给 AIService 处理
                    listener?.onAIRequest(text, taskType,null) { resultText ->
                        hideAiLoading()
                        showAIResultDialog(taskType, resultText, -1,-1)
                    }
                }
                else ->{
                    val start = etContent.selectionStart
                    val end = etContent.selectionEnd
                    if (start < end) {
                        val text = etContent.text.substring(start, end)
                        listener?.onAIRequest(text, taskType, null) { resultText ->
                            // 如果 View 已经不在窗口上了，就不处理回调了
                            if (!isAttachedToWindow) return@onAIRequest

                            hideAiLoading()
                            showAIResultDialog(taskType, resultText, start, end)
                        }
                    }else{
                        hideAiLoading()
                    }
                }
            }
        }
    }

    private fun showAIResultDialog(taskType: TaskType, result: String, start: Int, end: Int) {
        val activity = context as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            // 如果 Activity 正在销毁或已销毁，直接停止，不弹窗
            return
        }
        val title = when (taskType) {
            TaskType.CORRECT -> "纠错建议"
            TaskType.POLISH -> "润色结果"
            TaskType.TRANSLATE -> "翻译结果"
            TaskType.SUMMARY -> "摘要"
        }
        if(taskType == TaskType.SUMMARY){
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(result)
                .setPositiveButton("确认") { _, _ ->
                    listener?.onUpdateAbstract(result)
                }
                .setNegativeButton("复制") { _, _ ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("AI Result", result)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("取消", null)
                .show()
            return
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(result)
            .setPositiveButton("替换原文") { _, _ ->
                // 调用 Controller 的新方法
                controller?.performReplace(start, end, result)
            }
            .setNegativeButton("复制") { _, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("AI Result", result)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("取消", null)
            .show()
    }


    /**
     * 关键优化：View 附着到窗口时，自动查找生命周期并初始化
     * 这样使用者就不需要在 Adapter 里手动传 LifecycleOwner 了
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (controller == null) {
            val owner = findViewTreeLifecycleOwner()
            if (owner != null) {
                initController(owner)
            }
        }
    }

    private fun initController(owner: LifecycleOwner) {
        if (controller != null) return

        val scope = owner.lifecycleScope
        controller = RichTextController(etContent, scope).apply {
            onContentChanged = { html ->
                listener?.onContentChanged(html)
            }
        }


        pendingHtml?.let {
            html = it
            pendingHtml = null
        }
    }

    private fun setupClickListeners() {
        // 样式操作直接调用 Controller
        btnBold.setOnClickListener { safeExec { toggleBold() } }
        btnItalic.setOnClickListener { safeExec { toggleItalic() } }
        btnCancel.setOnClickListener { safeExec { undo() } }
        btnRecover.setOnClickListener { safeExec { redo() } }

        // 交互操作回调给外部
        btnAddPicture.setOnClickListener {
            if (!isReadOnly()) listener?.onInsertImageRequest()
        }

        btnSave.setOnClickListener {
            val currentHtml = controller?.exportHtml() ?: ""
            listener?.onSave(currentHtml)
        }
        btnStopAi.setOnClickListener {
            // 1. 取消具体的网络请求任务
            aiJob?.cancel()
            // 2. 隐藏 Loading
            hideAiLoading()
            //Toast.makeText(this, "已取消生成", Toast.LENGTH_SHORT).show()
        }
    }

    // 显示方法
    private fun showAiLoading() {
        // 使用简单的淡入动画
        aiLoadingCard.alpha = 0f
        aiLoadingCard.visibility = View.VISIBLE
        aiLoadingCard.animate().alpha(1f).setDuration(200).start()

        // 如果你想更高级，可以让 EditText 暂时不可编辑（只读），防止用户乱改导致插入错位
        // etContent.isEnabled = false // 可选：看你的需求
    }

    // 隐藏方法
    private fun hideAiLoading() {
        aiLoadingCard.animate().alpha(0f).setDuration(200).withEndAction {
            aiLoadingCard.visibility = View.GONE
        }.start()

        // 恢复编辑
        // etContent.isEnabled = true
    }

    // 辅助方法：确保 Controller 存在且非只读时执行
    private inline fun safeExec(block: RichTextController.() -> Unit) {
        if (!isReadOnly()) {
            controller?.block()
        }
    }

    private fun isReadOnly() = controller?.isReadOnlyMode == true

    private fun updateUIForReadOnly(readOnly: Boolean) {
        // 1. 设置 EditText 属性
        etContent.keyListener = if (readOnly) null else android.text.method.TextKeyListener.getInstance()
        etContent.isFocusable = !readOnly
        etContent.isFocusableInTouchMode = !readOnly

        if (readOnly) {
            etContent.movementMethod = LinkMovementMethod.getInstance()
            etContent.clearFocus()
            // 强制收起键盘
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.hideSoftInputFromWindow(windowToken, 0)
        } else {
            etContent.movementMethod = ArrowKeyMovementMethod.getInstance()
        }

        // 2. 更新按钮视觉状态
        val alpha = if (readOnly) 0.3f else 1.0f
        val buttons = listOf(btnBold, btnItalic, btnAddPicture, btnCancel, btnRecover)
        buttons.forEach { it.alpha = alpha }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }
    }





}