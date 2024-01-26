package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R.layout.login
import com.google.android.gms.auth.api.signin.GoogleSignIn

class Login : ComponentActivity() {

    private lateinit var firebaseOperations: FirebaseOperations
    private lateinit var textViewGoogleNick: EditText
    private lateinit var buttonNickG: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(login)

        firebaseOperations = FirebaseOperations(this)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)

        if (firebaseOperations.getCurrentUser() != null || userEmail != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return
        }

        val registerText: TextView = findViewById(R.id.textView_create_now)
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val googleSignUp: TextView = findViewById(R.id.textViewGoogleL)
        googleSignUp.setOnClickListener {
            signInGoogle()
        }

        val changePassword: TextView = findViewById(R.id.textViewForgotPassword)
        changePassword.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        textViewGoogleNick = findViewById(R.id.textViewGoogleNick)
        buttonNickG = findViewById(R.id.buttonNickG)
        buttonNickG.visibility = GONE
        textViewGoogleNick.visibility = GONE

        val loginButton: Button = findViewById(R.id.buttonLoginL)
        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun signInGoogle() {
        val signIntent = firebaseOperations.getClient().signInIntent
        launcher.launch(signIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            firebaseOperations.signInWithGoogle(task) { userExists, hasLogin ->
                if (userExists) {

                    if (!hasLogin) {
                        // Pokaż pole loginu i przycisk "Confirm"
                        showLoginFields()
                    } else {
                        // Przekieruj do MainActivity, jeśli użytkownik ma już ustawiony login
                        moveToMainActivity()
                        showToast("Logged in")
                    }
                } else {
                    firebaseOperations.showToast("Error")
                }
            }
        }
    }


    private fun showLoginFields() {
        textViewGoogleNick.visibility = View.VISIBLE
        buttonNickG.visibility = View.VISIBLE

        findViewById<TextView>(R.id.textViewLoginText).visibility = GONE
        findViewById<EditText>(R.id.editTextLoginL).visibility = GONE
        findViewById<EditText>(R.id.editTextPasswordL).visibility = GONE
        findViewById<TextView>(R.id.textView_create_now).visibility = GONE
        findViewById<Button>(R.id.buttonLoginL).visibility = GONE
        findViewById<TextView>(R.id.textViewGoogleL).visibility = GONE
        findViewById<ImageView>(R.id.imageView3).visibility = GONE
        findViewById<TextView>(R.id.textViewForgotPassword).visibility = GONE


        // Ustaw obsługę dla przycisku "Confirm"
        buttonNickG.setOnClickListener {
            val loginText = textViewGoogleNick.text.toString()
            if (loginText.isNotEmpty()) {
                firebaseOperations.checkIfLoginIsUnique(loginText) { isUnique ->
                    if (isUnique) {
                        firebaseOperations.saveLoginToSharedPreferences(firebaseOperations.getCurrentUser()?.email ?: "")
                        firebaseOperations.saveToUsersCollection(loginText, firebaseOperations.getCurrentUser()?.email ?: "") {
                            // Callback after saving to user's collection
                            moveToMainActivity()
                        }
                    } else {
                        showToast("Login is not unique. Choose a different login.")
                    }
                }
            } else {
                showToast("Please fill the login field")
            }
        }
    }

    private fun performLogin() {
        val email = findViewById<EditText>(R.id.editTextLoginL)
        val password = findViewById<EditText>(R.id.editTextPasswordL)



        if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
            showToast("Please fill all fields")
            return
        }
        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()

        firebaseOperations.signInWithEmailAndPassword(inputEmail, inputPassword) { success ->
            if (success) {
                firebaseOperations.saveLoginToSharedPreferences(inputEmail)
                moveToMainActivity()
                showToast("Success.")
            } else {
                // Handle failure
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
