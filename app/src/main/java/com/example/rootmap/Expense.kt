package com.example.rootmap

data class Expense(
    val name: String,
    var spending: String, // var로 변경하여 값을 수정 가능하도록 함
    var day: String
)

