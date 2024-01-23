package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btn: Button
    private lateinit var btnTotal:Button
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        btn = findViewById(R.id.Sign_out_b)
        btn.setOnClickListener {
            // Wyczyść SharedPreferences
            clearSharedPreferences()

            // Wyloguj się z Firebase
            FirebaseAuth.getInstance().signOut()

            // Wyloguj się z Google (jeżeli jesteś zalogowany przy użyciu Google Sign-In)
            googleSignInClient.signOut()

            // Przekieruj do ekranu logowania
            redirectToLogin()

            // Zakończ bieżącą aktywność
            finish()
        }

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
                    } else {
                        val welcomeText = "Witaj"
                        welcomeMessage.text = welcomeText
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd odczytu z Firestore
                    // W praktyce warto dodać odpowiednie logi lub obsługę błędów
                }
            btnTotal=findViewById(R.id.buttonIncreaseTotal)
            btnTotal.setOnClickListener {
                val userDocumentRef = firestore.collection("users").document(uid)
                val word1Ref = userDocumentRef.collection("stats")
                    .document("word_stats")
                    .collection("categories")
                    .document("animals")
                    .collection("words")
                    .document("word1")
                word1Ref.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val currentTotal = documentSnapshot.getLong("total") ?: 0

                        // Zaktualizuj wartość "total" w dokumencie word1
                        val newTotal = currentTotal + 1
                        word1Ref.update("total", newTotal)
                            .addOnSuccessListener {
                                // Jeżeli aktualizacja zakończyła się sukcesem
                                // Tutaj możesz dodać odpowiednią logikę lub powiadomienie
                            }
                            .addOnFailureListener { exception ->
                                // Obsłuż błąd aktualizacji
                                // W praktyce warto dodać odpowiednie logi lub obsługę błędów
                            }
                    }
                }

            }

        } else {
            // Przekieruj do ekranu logowania, jeśli użytkownik nie jest zalogowany
            redirectToLogin()
        }

    }

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun clearSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }
}
