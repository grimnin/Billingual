package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val welcomeMessage: TextView = findViewById(R.id.welcomeMessage)

        // Sprawdź, czy użytkownik jest zalogowany
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Uzyskaj UID zalogowanego użytkownika
            val uid: String = currentUser.uid

            // Uzyskaj login z Firestore na podstawie UID
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Ustaw tekst zalogowanego użytkownika
                        val login: String = document.getString("login") ?: ""
                        val welcomeText = "Witaj $login"
                        welcomeMessage.text = welcomeText
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd odczytu z Firestore
                    // W praktyce warto dodać odpowiednie logi lub obsługę błędów
                }
        }
    }
}
