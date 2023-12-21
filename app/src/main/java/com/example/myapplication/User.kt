package com.example.myapplication

data class User(
    val uid: String,
    val email: String,
    val login: String
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "login" to login
        )
    }
}
