package com.example.myapplication.Model.data

data class IrregularVerb(
    val base: String,
    val pastSimple: String,
    val pastParticiple: String,
    val meaning: String,
    val id:String,
    val correctAnswers:Int,
    val wrongAnswers:Int,
    val madeMistake:Boolean

)