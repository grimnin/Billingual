package com.example.myapplication.fragments.quiz

data class Word(
    val pl: String = "",
    val eng: String = "",
    val correctCount: Int = 0,
    val mistakeCounter: Int = 0,
    val total: Int = 0,
    val madeMistake: Boolean = false
)
