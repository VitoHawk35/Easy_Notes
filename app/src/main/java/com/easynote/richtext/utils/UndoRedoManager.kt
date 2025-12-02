package com.easynote.richtext.utils


import com.easynote.richtext.operation.Operation
import com.easynote.richtext.operation.OperationType
import java.util.ArrayDeque

class UndoRedoManager {
    // 使用 ArrayDeque 作为栈
    private val cancel_stack = ArrayDeque<Operation>()
    private val recover_stack = ArrayDeque<Operation>()

    // 最大历史记录步数（防止内存无限膨胀），Word 通常也有上限
    private val MAX_HISTORY_SIZE = 50

    /**
     * 用户执行了一个新操作（打字、删除等）
     */
    fun addOperation(op: Operation) {
        cancel_stack.push(op)
        // 关键：一旦有新操作，重做栈必须清空
        recover_stack.clear()

        // 可选：限制栈大小
        if (cancel_stack.size > MAX_HISTORY_SIZE) {
            cancel_stack.removeLast() // 移除最旧的记录
        }
    }

    /**
     * 执行撤销
     * @return 需要执行的逆向操作，如果无法撤销返回 null
     */
    fun cancel(): Operation? {
        if (cancel_stack.isEmpty()) return null

        val op = cancel_stack.pop()
        recover_stack.push(op) // 移入重做栈

        // 返回逆向操作供编辑器执行
        return getInverseOperation(op)
    }

    /**
     * 执行重做
     * @return 需要再次执行的操作，如果无法重做返回 null
     */
    fun recover(): Operation? {
        if (recover_stack.isEmpty()) return null

        val op = recover_stack.pop()
        cancel_stack.push(op) // 移回撤销栈

        // 重做就是再执行一次原操作
        return op
    }

    fun clear(){
        cancel_stack.clear()
        recover_stack.clear()
    }

    // 辅助状态检查
    fun canUndo() = !cancel_stack.isEmpty()
    fun canRedo() = !recover_stack.isEmpty()

    /**
     * 生成逆向逻辑
     * 比如：原操作是"在位置0插入ABC"，逆向操作就是"在位置0删除ABC"
     */
    private fun getInverseOperation(op: Operation): Operation {
        return when (op.operation) {
            OperationType.ADD -> op.copy(operation = OperationType.DELETE)
            OperationType.DELETE -> op.copy(operation = OperationType.ADD)
            OperationType.BOLD -> op.copy(operation = OperationType.CANCEL_BOLD)
            OperationType.ITALIC -> op.copy(operation = OperationType.CANCEL_ITALIC)
            OperationType.IMAGE -> op.copy(operation = OperationType.CANCEL_IMAGE)
            OperationType.CANCEL_ITALIC -> op.copy(operation = OperationType.ITALIC)
            OperationType.CANCEL_BOLD -> op.copy(operation = OperationType.BOLD)
            OperationType.CANCEL_IMAGE -> op.copy(operation = OperationType.IMAGE)
            OperationType.BATCH -> {
                // 1. 先把每个子操作取反
                val inverseSubOps = op.subOperations?.map { getInverseOperation(it) } ?: emptyList()

                op.copy(subOperations = inverseSubOps.reversed())
            }
        }
    }
}