package com.easynote.richtext.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
/**
 * 高性能版：支持长文本，后台计算 Diff，智能跳过无用前缀
 * * @param shouldIgnore 一个返回 Boolean 的函数。如果返回 true，TextWatcher 会更新内部状态但不会触发回调。
 * 用于防止撤销/重做操作触发无限循环。
 */
fun EditText.monitorBatchDiff(
    scope: CoroutineScope,
    debounceTime: Long = 400L,
    shouldIgnore: () -> Boolean = { false }, // 🆕 新增参数，默认为 false
    action: (isInput: Boolean, diffContent: String, startPos: Int) -> Unit
) {
    // 将 shouldIgnore 传给 Watcher
    val watcher = DiffBatchTextWatcher(scope, debounceTime, shouldIgnore, action)
    watcher.attachTo(this)
}

class DiffBatchTextWatcher(
    private val scope: CoroutineScope,
    private val debounceTime: Long = 400L,
    private val shouldIgnore: () -> Boolean, // 🆕 新增属性
    private val onBatchResult: (Boolean, String, Int) -> Unit
) : TextWatcher{

    private var searchJob: Job? = null
    private var lastStableText: String = ""

    // 💡 优化核心 1: 记录这一波操作中，最早发生变动的位置
    // 初始值为极大值
    private var minModifiedStart = Int.MAX_VALUE
    private var isFirstAttach = true

    fun attachTo(editText: EditText) {
        lastStableText = editText.text.toString()
        isFirstAttach = false
        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (isFirstAttach) lastStableText = s.toString()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // 🔥🔥🔥 核心修复代码开始 🔥🔥🔥
        // 如果外部告诉我们要忽略这次变化（比如正在执行撤销）
        if (shouldIgnore()) {
            // 关键：虽然忽略回调，但必须更新 lastStableText！
            // 否则下一次用户真正输入时，会拿现在的文本和“很久以前”的旧文本比对，导致 Diff 错误。
            lastStableText = s.toString()

            // 重置扫描游标，因为文本已经更新了
            minModifiedStart = Int.MAX_VALUE

            // 取消之前的任务，防止之前的延迟任务这时候醒来
            searchJob?.cancel()

            // 直接结束，不启动协程，不触发回调
            return
        }
        // 🔥🔥🔥 核心修复代码结束 🔥🔥🔥

        // 💡 优化核心 1: 更新最小变动点
        // 比如用户先在第100位输入，又去第50位删除。最小变动点就是 50。
        // 0-49位我们完全不需要去比对。
        minModifiedStart = min(minModifiedStart, start)

        searchJob?.cancel()
        searchJob = scope.launch {
            delay(debounceTime) // 防抖等待

            val currentText = s.toString()

            // 如果文本没变，直接重置
            if (currentText == lastStableText) {
                minModifiedStart = Int.MAX_VALUE
                return@launch
            }

            // 捕获不可变的数据，准备传给后台线程
            val oldTextSnapshot = lastStableText
            val startScanIndex = minModifiedStart

            // 💡 优化核心 2: 切换到后台线程进行耗时计算
            val result = withContext(Dispatchers.Default) {
                calculateDiffOptimized(oldTextSnapshot, currentText, startScanIndex)
            }

            // 切回主线程更新 UI 和状态
            if (result != null) {
                lastStableText = currentText // 更新锚点
                minModifiedStart = Int.MAX_VALUE // 重置扫描游标

                // 回调结果
                onBatchResult(result.isInput, result.diffContent, result.startPosition)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    data class BatchResult(val isInput: Boolean, val diffContent: String, val startPosition: Int)

    private fun calculateDiffOptimized(oldText: String, newText: String, optimizeStart: Int): BatchResult? {
        val oldLen = oldText.length
        val newLen = newText.length

        // 1. 寻找公共前缀 (Prefix)
        // 🚀 核心优化：直接从 optimizeStart 开始找，之前的绝对一样！
        // 安全检查：防止 optimizeStart 越界（虽然逻辑上不应该，但防御性编程很重要）
        var p = optimizeStart.coerceIn(0, min(oldLen, newLen))

        // 有可能 optimizeStart 之前的某些字符被后续操作影响了吗？
        // 理论上 minModifiedStart 保证了它之前没变。
        // 但为了 100% 稳健，我们可以稍微回退一点点或做个简单的双重检查，
        // 不过在这里，我们相信 TextWatcher 的 start 参数。

        // 修正逻辑：必须确保从p开始确实相等，万一之前计算有误，这里while循环会自动处理
        // 实际上，为了兼容性，我们可以让 p 从 optimizeStart 开始，
        // 但如果 optimizeStart 很大，我们几乎跳过了整个字符串的遍历。

        // 如果用户在最后追加，optimizeStart 就是 oldLen。p 直接从最后开始，瞬间结束。
        while (p < oldLen && p < newLen && oldText[p] == newText[p]) {
            p++
        }

        // 2. 寻找公共后缀 (Suffix)
        var sOld = oldLen - 1
        var sNew = newLen - 1
        while (sOld >= p && sNew >= p && oldText[sOld] == newText[sNew]) {
            sOld--
            sNew--
        }

        if (sNew < p && sOld < p) return null // 无差异

        return when {
            newLen > oldLen -> {
                // Input
                BatchResult(true, newText.substring(p, sNew + 1), p)
            }
            oldLen > newLen -> {
                // Delete
                BatchResult(false, oldText.substring(p, sOld + 1), p)
            }
            else -> {
                // Replacement (treated as Input)
                BatchResult(true, newText.substring(p, sNew + 1), p)
            }
        }
    }
}