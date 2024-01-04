package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R.layout.login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignUp: TextView
    private lateinit var client: GoogleSignInClient
    private lateinit var textViewGoogleNick: EditText
    private lateinit var buttonNickG: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(login)
        auth = Firebase.auth

        // Sprawdź, czy istnieją zapisane dane logowania
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)

        // Sprawdź, czy użytkownik jest już zalogowany
        if (auth.currentUser != null || userEmail != null) {
            // Użytkownik jest już zalogowany, przejdź do MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        val registerText: TextView = findViewById(R.id.textView_create_now)
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val googleSignUp: TextView = findViewById(R.id.textViewGoogleL)
        googleSignUp.setOnClickListener {
            signInGoogle()
        }

        textViewGoogleNick = findViewById(R.id.textViewGoogleNick)
        buttonNickG = findViewById(R.id.buttonNickG)
        buttonNickG.visibility = View.GONE
        textViewGoogleNick.visibility = View.GONE

        val loginButton: Button = findViewById(R.id.buttonLoginL)
        loginButton.setOnClickListener {
            preformLogin()
        }

        val confirmButtonG: Button = findViewById(R.id.buttonNickG)
        confirmButtonG.setOnClickListener {
            val loginText = textViewGoogleNick.text.toString()
            if (loginText.isNotEmpty()) {
                checkIfLoginIsUnique(loginText) { isUnique ->
                    if (isUnique) {
                        saveLoginToSharedPreferences(auth.currentUser?.email ?: "")
                        saveToUsersCollection(loginText, auth.currentUser?.email ?: "")
                    } else {
                        Toast.makeText(
                            this,
                            "Login is not unique. Choose a different login.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill the login field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInGoogle() {
        val signIntent = client.signInIntent
        launcher.launch(signIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val email = account.email
                if (email != null) {
                    checkIfUserExistsInDatabase(email) { userExists ->
                        if (!userExists) {
                            textViewGoogleNick.visibility = View.VISIBLE
                            buttonNickG.visibility = View.VISIBLE

                            findViewById<EditText>(R.id.editTextLoginL).visibility = View.GONE
                            findViewById<EditText>(R.id.editTextPasswordL).visibility = View.GONE
                            findViewById<Button>(R.id.buttonLoginL).visibility = View.GONE
                            findViewById<TextView>(R.id.textView_create_now).visibility = View.GONE
                            findViewById<TextView>(R.id.textViewGoogleL).visibility = View.GONE
                            findViewById<ImageView>(R.id.imageView3).visibility = View.GONE

                            buttonNickG.setOnClickListener {
                                val loginText = textViewGoogleNick.text.toString()
                                if (loginText.isNotEmpty()) {
                                    checkIfLoginIsUnique(loginText) { isUnique ->
                                        if (isUnique) {
                                            saveLoginToSharedPreferences(email)
                                            saveToUsersCollection(loginText, email)
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Login is not unique. Choose a different login.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Please fill the login field",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Użytkownik już istnieje, przechodź do MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
                }
            } else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLoginToSharedPreferences(userEmail: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userEmail", userEmail)
        editor.apply()
    }

    private fun saveToUsersCollection(login: String, email: String) {
        val userMap = hashMapOf(
            "email" to email,
            "login" to login
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(auth.currentUser?.uid ?: "")
            .set(userMap)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    baseContext,
                    "Error creating user document: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun preformLogin() {
        val email = findViewById<EditText>(R.id.editTextLoginL)
        val password = findViewById<EditText>(R.id.editTextPasswordL)
        if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()

        auth.signInWithEmailAndPassword(inputEmail, inputPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && auth.currentUser?.isEmailVerified == true) {
                    saveLoginToSharedPreferences(inputEmail)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    Toast.makeText(
                        baseContext, "Success.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (auth.currentUser?.isEmailVerified == false) {
                    Toast.makeText(
                        baseContext, "You haven't verified your account via mailbox yet.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    baseContext, "Authentication failed. ${it.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkIfUserExistsInDatabase(email: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback.invoke(false)
                } else {
                    callback.invoke(true)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext,
                    "Error checking user existence: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()

                callback.invoke(false)
            }
    }

    private fun checkIfLoginIsUnique(login: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection
            .whereEqualTo("login", login)
            .get()
            .addOnSuccessListener { documents ->
                callback.invoke(documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext,
                    "Error checking login uniqueness: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()

                callback.invoke(false)
            }
    }
}
