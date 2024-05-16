package com.example.myapplication.Model.data

data class WrongAnswerWords(
    val pl: String = "",
    val eng: String = "",
    val correctCount: Int = 0,
    val mistakeCounter: Int = 0,
    val total: Int = 0,
    val madeMistake: Boolean = false,
    val id: String =""
)

