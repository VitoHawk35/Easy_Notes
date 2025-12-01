package com.easynote.richtext.utils

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.easynote.richtext.view.impl.RichTextController
import com.example.mydemo.ai.core.AIProvider
import com.example.mydemo.ai.core.TaskType
import com.example.mydemo.ai.model.Response.ChatCompletionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 负责 AI 功能的执行与交互 (业务逻辑)
 */
class AIService(
    private val context: Context,
    private val controller: RichTextController
) {

    fun performAITask(taskType: TaskType, selectedText: String, start: Int, end: Int) {
        Toast.makeText(context, "AI正在处理...", Toast.LENGTH_SHORT).show()

        AIProvider.getInstance().process(selectedText, taskType, object : Callback<ChatCompletionResponse> {
            override fun onResponse(call: Call<ChatCompletionResponse>, response: Response<ChatCompletionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val choices = response.body()?.choices
                    if (!choices.isNullOrEmpty()) {
                        val resultText = choices[0].message.content
                        showAIResultDialog(taskType, resultText, start, end)
                    } else {
                        showToast("AI返回内容为空")
                    }
                } else {
                    showToast("AI请求失败: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatCompletionResponse>?, t: Throwable) {
                showToast("网络错误: ${t.message}")
            }
        })
    }

    private fun showAIResultDialog(taskType: TaskType, result: String, start: Int, end: Int) {
        val title = when (taskType) {
            TaskType.CORRECT -> "纠错建议"
            TaskType.POLISH -> "润色结果"
            TaskType.TRANSLATE -> "翻译结果"
            TaskType.SUMMARY -> "摘要"
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(result)
            .setPositiveButton("替换原文") { _, _ ->
                // 调用 Controller 的新方法
                controller.performReplace(start, end, result)
            }
            .setNegativeButton("复制") { _, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("AI Result", result)
                clipboard.setPrimaryClip(clip)
                showToast("已复制到剪贴板")
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}