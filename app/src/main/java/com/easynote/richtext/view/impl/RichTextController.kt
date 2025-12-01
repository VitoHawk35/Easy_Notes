package com.easynote.richtext.view.impl

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.widget.EditText
import com.easynote.richtext.operation.DoOperation
import com.easynote.richtext.operation.Operation
import com.easynote.richtext.operation.OperationType
import com.easynote.richtext.utils.HtmlConverter
import com.easynote.richtext.utils.ImageUtils
import com.easynote.richtext.utils.UndoRedoManager
import com.easynote.richtext.utils.monitorBatchDiff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 核心逻辑控制器：负责处理富文本的所有非 UI 逻辑
 * 包括：撤销重做、输入监听、样式应用、HTML 转换
 */
class RichTextController(
    private val etContent: EditText,
    private val scope: CoroutineScope
) {
    private val context: Context get() = etContent.context
    private val undoRedoManager = UndoRedoManager()

    // 标记状态
    var isReadOnlyMode = false
    private var isUndoingOrRedoing = false

    // 逻辑组件
    private val doOperation = DoOperation(etContent) {
        // 提供宽度计算逻辑
        if (etContent.width > 0) etContent.width - etContent.paddingLeft - etContent.paddingRight else 1000
    }

    // 临时存储即将被删除的图片的 URI
    private val deletedImageMap = mutableMapOf<Int, String>()

    // 内容变更回调（用于同步）
    var onContentChanged: ((String) -> Unit)? = null

    init {
        setupImageDeletionCapture() // 监听图片删除
        startMonitoringInput()      // 开启输入监听 (monitorBatchDiff)
        setupSyncListener()         // 开启内容同步
    }

    // ================== 公开操作接口 ==================

    fun toggleBold() {
        toggleStyle(Typeface.BOLD, OperationType.BOLD, OperationType.CANCEL_BOLD)
    }

    fun toggleItalic() {
        toggleStyle(Typeface.ITALIC, OperationType.ITALIC, OperationType.CANCEL_ITALIC)
    }

    fun undo() {
        performUndoRedo { undoRedoManager.cancel() }
    }

    fun redo() {
        performUndoRedo { undoRedoManager.recover() }
    }

    fun insertImage(uri: Uri) {
        val start = etContent.selectionStart
        val insertPos = if (start < 0) etContent.length() else start

        // 使用协程处理图片拷贝，避免阻塞主线程
        scope.launch(Dispatchers.IO) {
            // 将图片拷贝到 App 私有目录，防止原图被删导致显示不出来
            val localPath = ImageUtils.copyImageToAppStorage(context, uri) ?: uri.toString()

            withContext(Dispatchers.Main) {
                val op = Operation(
                    start = insertPos,
                    end = insertPos + 1,
                    operation = OperationType.IMAGE,
                    text = localPath
                )
                // 执行插入并入栈
                performUndoRedo {
                    undoRedoManager.addOperation(op)
                    op
                }
            }
        }
    }

    fun exportHtml(): String {
        return HtmlConverter.toHtml(etContent.text)
    }

    fun loadHtml(html: String) {
        // 计算合适的宽度给图片自适应
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        var targetWidth = if (etContent.width > 0) etContent.width else screenWidth
        targetWidth = targetWidth - etContent.paddingLeft - etContent.paddingRight
        if (targetWidth <= 0) targetWidth = 1000

        val spanned = HtmlConverter.fromHtml(html, context, targetWidth)

        isUndoingOrRedoing = true
        try {
            etContent.setText(spanned)
            etContent.setSelection(etContent.length())
            undoRedoManager.clear()
        } finally {
            isUndoingOrRedoing = false
            // 加载不视为用户编辑，通常不需要触发 onContentChanged，或者根据需求触发
            // onContentChanged?.invoke(exportHtml())
        }
    }

    // ================== 私有核心逻辑 ==================

    private fun startMonitoringInput() {
        // 原 monitorBatchDiff 逻辑
        etContent.monitorBatchDiff(
            scope = scope,
            shouldIgnore = { isUndoingOrRedoing }
        ) { isInput, content, pos ->
            if (isInput) {
                undoRedoManager.addOperation(Operation(start = pos, end = pos + content.length, operation = OperationType.ADD, text = content))
            } else {
                // 智能拆分删除操作
                if (content.contains("\uFFFC")) {
                    handleComplexDeletion(pos, content)
                } else {
                    undoRedoManager.addOperation(Operation(start = pos, end = pos + content.length, operation = OperationType.DELETE, text = content))
                }
            }
        }
    }

    /**
     * 辅助封装：执行撤销/重做或新操作
     */
    private fun performUndoRedo(action: () -> Operation?) {
        isUndoingOrRedoing = true
        try {
            val op = action()
            doOperation.doOperation(op)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isUndoingOrRedoing = false
            onContentChanged?.invoke(exportHtml())
        }
    }

    private fun toggleStyle(style: Int, applyOp: OperationType, cancelOp: OperationType) {
        val start = etContent.selectionStart
        val end = etContent.selectionEnd
        if (start < 0 || end < 0 || start == end) return

        val opType = if (isSelectionFullyStyled(start, end, style)) cancelOp else applyOp
        val op = Operation(start = start, end = end, operation = opType)

        performUndoRedo {
            undoRedoManager.addOperation(op)
            op
        }
    }

    private fun setupSyncListener() {
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUndoingOrRedoing) {
                    onContentChanged?.invoke(exportHtml())
                }
            }
        })
    }

    // 逻辑完全从 RichTextView 迁移过来
    private fun setupImageDeletionCapture() {
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isUndoingOrRedoing) return
                if (count > 0 && s is Spannable) {
                    val end = start + count
                    val imageSpans = s.getSpans(start, end, ImageSpan::class.java)
                    if (imageSpans.isNotEmpty()) {
                        deletedImageMap.clear()
                        for (span in imageSpans) {
                            val spanStart = s.getSpanStart(span)
                            val source = span.source
                            if (source != null) {
                                deletedImageMap[spanStart] = source
                            }
                        }
                    }
                }
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 逻辑完全从 RichTextView 迁移过来
    private fun handleComplexDeletion(startPos: Int, content: String) {
        val subOps = mutableListOf<Operation>()
        var currentRelPos = 0
        val buffer = StringBuilder()

        for (i in content.indices) {
            val char = content[i]
            if (char == '\uFFFC') {
                if (buffer.isNotEmpty()) {
                    val text = buffer.toString()
                    val absStart = startPos + currentRelPos
                    subOps.add(Operation(start = absStart, end = absStart + text.length, operation = OperationType.DELETE, text = text))
                    currentRelPos += text.length
                    buffer.clear()
                }

                val absStart = startPos + currentRelPos
                val uri = deletedImageMap[absStart]

                if (uri != null) {
                    subOps.add(Operation(start = absStart, end = absStart + 1, operation = OperationType.CANCEL_IMAGE, text = uri))
                } else {
                    subOps.add(Operation(start = absStart, end = absStart + 1, operation = OperationType.DELETE, text = "\uFFFC"))
                }
                currentRelPos += 1

            } else {
                buffer.append(char)
            }
        }

        if (buffer.isNotEmpty()) {
            val text = buffer.toString()
            val absStart = startPos + currentRelPos
            subOps.add(Operation(start = absStart, end = absStart + text.length, operation = OperationType.DELETE, text = text))
        }

        subOps.reverse()

        if (subOps.isNotEmpty()) {
            val batchOp = Operation(
                start = startPos,
                end = startPos + content.length,
                operation = OperationType.BATCH,
                subOperations = subOps
            )
            undoRedoManager.addOperation(batchOp)
        }
    }

    // 逻辑完全从 RichTextView 迁移过来
    private fun isSelectionFullyStyled(start: Int, end: Int, style: Int): Boolean {
        if (start == end) return false
        val editable = etContent.text
        val spans = editable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == style }
            .sortedBy { editable.getSpanStart(it) }

        if (spans.isEmpty()) return false
        var currentPos = start
        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            if (spanStart > currentPos) return false
            if (spanEnd > currentPos) {
                currentPos = spanEnd
            }
            if (currentPos >= end) return true
        }
        return currentPos >= end
    }

    // ================== 新增方法 ==================

    /**
     * 执行替换操作（专门用于 AI 替换、查找替换等场景）
     * 手动构建撤销记录，避免 TextWatcher 识别错误
     */
    fun performReplace(start: Int, end: Int, newText: String) {
        if (start < 0 || end > etContent.length() || start > end) return

        // 1. 获取即将被删除的旧文本
        val oldText = etContent.text.substring(start, end)

        // 2. 构建复合操作 (BATCH)
        // 子操作1: 删除旧文本
        val deleteOp = Operation(start = start, end = end, operation = OperationType.DELETE, text = oldText)
        // 子操作2: 插入新文本
        val addOp = Operation(start = start, end = start + newText.length, operation = OperationType.ADD, text = newText)

        val batchOp = Operation(
            start = start,
            end = start + newText.length, // 这里的 end 实际上对于 batch 意义不大，取最终态即可
            operation = OperationType.BATCH,
            subOperations = listOf(deleteOp, addOp) // 注意顺序：先删后加
        )

        // 3. 将操作加入撤销栈
        undoRedoManager.addOperation(batchOp)

        // 4. 执行 UI 更新（关键：要屏蔽 TextWatcher）
        isUndoingOrRedoing = true // 借用这个标志位，让 monitorBatchDiff 忽略本次回调
        try {
            etContent.text.replace(start, end, newText)
            etContent.setSelection(start + newText.length) // 移动光标到末尾
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isUndoingOrRedoing = false
            // 手动触发一次内容变更回调
            onContentChanged?.invoke(exportHtml())
        }
    }
}