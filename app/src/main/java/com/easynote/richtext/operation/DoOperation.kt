package com.easynote.richtext.operation

import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.EditText
import com.easynote.richtext.utils.ImageUtils


class DoOperation(
    private val etContent: EditText,
    private val maxWidthProvider: () -> Int
) {

    /**
     * 执行单个或批量操作
     */
    fun doOperation(op: Operation?) {

            if (op == null) return

            // 递归处理 BATCH
            if (op.operation == OperationType.BATCH) {
                op.subOperations?.forEach { subOp ->
                    doOperation(subOp)
                }
                return
            }

            val editable = etContent.text
            val context = etContent.context

            when (op.operation) {
                OperationType.ADD -> {
                    editable.insert(op.start, op.text)
                    etContent.setSelection(op.start + op.text.length)
                }
                OperationType.DELETE -> {
                    if (op.end <= editable.length) {
                        editable.delete(op.start, op.end)
                        etContent.setSelection(op.start)
                    }
                }
                OperationType.BOLD -> {
                    editable.setSpan(StyleSpan(Typeface.BOLD), op.start, op.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                OperationType.ITALIC -> {
                    editable.setSpan(StyleSpan(Typeface.ITALIC), op.start, op.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                OperationType.CANCEL_BOLD -> {
                    removeStylePartially(op.start, op.end, Typeface.BOLD)
                }
                OperationType.CANCEL_ITALIC -> {
                    removeStylePartially(op.start, op.end, Typeface.ITALIC)
                }
                OperationType.IMAGE -> {
                    // 插入占位符逻辑
                    val needInsert = if (op.end > editable.length) true else !editable.substring(op.start, op.end).equals("\uFFFC")
                    if (needInsert) {
                        editable.insert(op.start, "\uFFFC")
                    }

                    try {
                        val uriStr = op.text
                        val uri = Uri.parse(uriStr)

                        // 获取外部提供的宽度
                        val maxWidth = maxWidthProvider()

                        val displayMetrics = context.resources.displayMetrics
                        val maxHeight = (displayMetrics.heightPixels * 0.5).toInt()

                        val bitmap = ImageUtils.loadScaledBitmap(context, uri, maxWidth, maxHeight)

                        if (bitmap != null) {
                            val drawable = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                            drawable.setBounds(0, 0, bitmap.width, bitmap.height)
                            val imageSpan = ImageSpan(drawable, uriStr)

                            editable.setSpan(imageSpan, op.start, op.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            etContent.setSelection(op.end)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Editor", "图片加载失败: ${e.message}")
                    }
                }
                OperationType.CANCEL_IMAGE -> {
                    if (op.end <= editable.length) {
                        editable.delete(op.start, op.end)
                        etContent.setSelection(op.start)
                    }
                }
                else -> {}
            }
    }

    /**
     * 辅助方法：局部移除样式
     */
    private fun removeStylePartially(start: Int, end: Int, typefaceStyle: Int) {
        val editable = etContent.text
        val spans = editable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == typefaceStyle }

        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            editable.removeSpan(span)

            if (spanStart < start) {
                editable.setSpan(StyleSpan(typefaceStyle), spanStart, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (spanEnd > end) {
                editable.setSpan(StyleSpan(typefaceStyle), end, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}