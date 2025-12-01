package com.easynote.richtext.operation

data class Operation(
    val start: Int,
    val end: Int,
    val operation: OperationType,
    val text: String="",
    val subOperations: List<Operation>? = null
)



