package com.easynote.richtext.utils

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri

object HtmlConverter {

    //保存为HTML
    fun toHtml(text: Spanned): String {
        // HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE 模式生成的 HTML 比较干净，适合编辑器
        return HtmlCompat.toHtml(text, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    }

    /**
     * 将 HTML 字符串转为 Spannable (回显用)
     * @param html HTML 内容
     * @param context 上下文
     * @param editorWidth 编辑器的宽度（用于图片自适应缩放）
     */
    fun fromHtml(html: String, context: Context, editorWidth: Int): Spanned {
        // ImageGetter 是核心：它负责解析 <img src="..."> 标签
        val imageGetter = Html.ImageGetter { source ->
            try {
                // 1. source 就是我们保存时的 uriString
                val uri = source.toUri()

                // 2. 计算最大高度限制 (屏幕一半)
                val displayMetrics = context.resources.displayMetrics
                val maxHeight = (displayMetrics.heightPixels * 0.5).toInt()

                // 3. 计算最大宽度 (传入的 editorWidth，如果未测量完成则给个默认值)
                val reqWidth = if (editorWidth > 0) editorWidth else 1000

                // 4. 复用 ImageUtils 加载图片
                val bitmap = ImageUtils.loadScaledBitmap(context, uri, reqWidth, maxHeight)

                if (bitmap != null) {
                    val drawable = bitmap.toDrawable(context.resources)
                    // 🔥 必须设置 setBounds，否则图片宽高为0，看不见
                    drawable.setBounds(0, 0, bitmap.width, bitmap.height)
                    return@ImageGetter drawable
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // 加载失败返回 null 或者一个错误的占位图
            null
        }

        // 转换
        return html.parseAsHtml(HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter)
    }
}