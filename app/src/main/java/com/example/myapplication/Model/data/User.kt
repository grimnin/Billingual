package com.example.myapplication.Model.data

data class User(
    val uid: String = "",
    val email: String = "",
    val login: String = "",
    val role:String="",
    val score: Int = 0 // Dodatkowe pole score
) {
    // Konstruktor bezargumentowy potrzebny dla deserializacji Firestore
    constructor() : this("", "", "","user", 0)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "login" to login,
            "role" to role,
            "score" to score // Dodajemy score do mapy
        )
    }
}
