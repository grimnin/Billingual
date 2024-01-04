package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class Register : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        auth = Firebase.auth

        val switchToLogin: Button = findViewById(R.id.buttonSwitchToLoginR)
        switchToLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val signUp: Button = findViewById(R.id.button_sign_up_R)
        signUp.setOnClickListener {
            performSignUp()
        }
    }

    private fun performSignUp() {
        val email = findViewById<EditText>(R.id.editTextEmailR)
        val password = findViewById<EditText>(R.id.editTextPasswordR)
        val login = findViewById<EditText>(R.id.editTextLoginR)

        if (email.text.toString().isEmpty() || password.text.toString().isEmpty() || login.text.toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()
        val inputLogin = login.text.toString()

        // Sprawdź, czy login jest unikalny
        checkIfLoginIsUnique(inputLogin) { isUnique ->
            if (isUnique) {
                // Jeśli login jest unikalny, kontynuuj proces rejestracji
                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Get the UID of the newly created user
                            val uid = auth.currentUser?.uid

                            // Create a map with additional user information (e.g., login)
                            val userMap = hashMapOf(
                                "email" to inputEmail,
                                "login" to inputLogin
                                // Dodaj inne informacje o użytkowniku, jeśli są dostępne
                            )

                            // Set additional user information in the Firebase Firestore
                            uid?.let {
                                FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        // Continue with the login code
                                        sendEmailVerification()
                                        val intent = Intent(this, Login::class.java)
                                        startActivity(intent)
                                        Toast.makeText(
                                            baseContext, "Success.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            baseContext,
                                            "Error saving user information: ${it.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            // Handle registration failure
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error occurred ${it.localizedMessage}", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                // Jeśli login nie jest unikalny, poinformuj użytkownika
                Toast.makeText(this, "Login is not unique. Choose a different login.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                }
            }
}
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun checkIfLoginIsUnique(login: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        // Sprawdź, czy istnieje użytkownik o podanym loginie
        usersCollection
            .whereEqualTo("login", login)
            .get()
            .addOnSuccessListener { documents ->
                // Jeśli nie ma dokumentów oznacza to, że login jest unikalny
                callback.invoke(documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                // Obsłuż błąd podczas sprawdzania unikalności loginu
                Toast.makeText(
                    baseContext,
                    "Error checking login uniqueness: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()

                // W przypadku błędu przekaż wynik (false) do callbacka
                callback.invoke(false)
            }
    }
}
