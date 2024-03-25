package com.example.myapplication.fragments.grammar

data class Sentence(
    val sentence: String,
    val zdanie: String,
    val tense:String,
    val id:String,
    val correctAnswers:Int,
    val wrongAnswers:Int,
    val madeMistake:Boolean
)
