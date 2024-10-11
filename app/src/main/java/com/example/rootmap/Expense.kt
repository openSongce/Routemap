package com.example.rootmap

data class Expense(
    val name: String,
    var spending: String,
    var day: String,
    var category: String = "" // 카테고리 필드 추가
)

