package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Register : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setConnfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        auth = FirebaseAuth.getInstance()
        // Odczytaj preferencje językowe


        val switchToLogin: Button = findViewById(R.id.buttonSwitchToLoginR)
        switchToLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val signUp: Button = findViewById(R.id.button_sign_up_R)
        signUp.setOnClickListener {
            performSignUp()
            clearSharedPreferences()
        }
    }

    private fun performSignUp() {
        val email = findViewById<EditText>(R.id.editTextEmailR)
        val password = findViewById<EditText>(R.id.editTextPasswordR)
        val login = findViewById<EditText>(R.id.editTextLoginR)

        if (email.text.toString().isEmpty() || password.text.toString().isEmpty() || login.text.toString().isEmpty()) {
            showToast("Please fill all fields")
            return
        }

        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()
        val inputLogin = login.text.toString()

        // Check if login is unique
        checkIfLoginIsUnique(inputLogin) { isUnique ->
            if (isUnique) {
                // If login is unique, proceed with creating an account
                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Get the UID of the newly created user
                            val uid = auth.currentUser?.uid

                            // Create a map with additional user information (e.g., login)
                            val userMap = hashMapOf(
                                "email" to inputEmail,
                                "login" to inputLogin,
                                "role" to "user",
                                "score" to 0,
                                "uid" to FirebaseAuth.getInstance().currentUser?.uid
                                // Add other user information if available
                            )

                            // Set additional user information in the Firebase Firestore
                            uid?.let {
                                FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        // Continue with the login code after successful registration
                                        sendEmailVerification()
                                        showToast("Success.")

                                        // Redirect the user to the login screen
                                        val intent = Intent(this, Login::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        showToast("Error saving user information: ${it.localizedMessage}")
                                    }
                            }
                        } else {
                            // Handle registration failure
                            showToast("Authentication failed.")
                        }
                    }
                    .addOnFailureListener {
                        showToast("Error occurred ${it.localizedMessage}")
                    }
            } else {
                // If login is not unique, inform the user
                showToast("Login is not unique. Choose a different login.")
            }
        }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseOperations(this).addWordStatsSubcollection(auth.currentUser?.uid ?: "")
                    FirebaseOperations(this).copyWordsToCategories(auth.currentUser?.uid ?: "")
                    showToast("Verification email sent to ${user.email}")
                } else {
                    showToast("Failed to send verification email")
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

        // Check if a user with the given login exists
        usersCollection
            .whereEqualTo("login", login)
            .get()
            .addOnSuccessListener { documents ->
                // If there are no documents, it means the login is unique
                callback.invoke(documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                // Handle the error while checking the uniqueness of the login
                showToast("Error checking login uniqueness: ${exception.localizedMessage}")

                // In case of an error, pass the result (false) to the callback
                callback.invoke(false)
            }
    }

    private fun clearSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
    }

    private fun loadLanguageFromSharedPreferences(): String {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("language", getString(R.string.default_language_code)) ?: getString(R.string.default_language_code)
    }
    private fun setConnfiguration(){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        setLocale(loadLanguageFromSharedPreferences())
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Ustaw odpowiedni styl w zależności od trybu ciemnego
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }
}
