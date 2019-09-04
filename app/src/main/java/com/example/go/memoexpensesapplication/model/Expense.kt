package com.example.go.memoexpensesapplication.model

import com.example.go.memoexpensesapplication.constant.RecyclerType

data class Expense(
    val type: RecyclerType,
    val tag: String?,
    val value: String?,
    val note: String?
) {
    constructor(
        tag: String?,
        value: String?,
        note: String?
    ): this(RecyclerType.BODY, tag, value, note)
}