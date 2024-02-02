package com.example.myapplication

import FirebaseOperations
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignUp: TextView
    private lateinit var client: GoogleSignInClient
    private lateinit var textViewGoogleNick: EditText
    private lateinit var buttonNickG: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        auth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)

        if (auth.currentUser != null && userEmail != null|| auth.currentUser?.isEmailVerified == true) {
            showToast("User already logged in.")
            navigateToMainActivity()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        val registerText: TextView = findViewById(R.id.textView_create_now)
        registerText.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        googleSignUp = findViewById(R.id.textViewGoogleL)
        googleSignUp.setOnClickListener {
            signInGoogle()
        }

        val changePassword: TextView = findViewById(R.id.textViewForgotPassword)
        changePassword.setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
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
            handleConfirmButton()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
            account?.let { updateUI(it) }
        } else {
            showToast("Google Sign-In failed: ${task.exception?.message}")
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                handleGoogleSignInSuccess(account)
            } else {
                showToast("Google Sign-In failed: ${task.exception?.message}")
            }
        }
    }

    private fun handleGoogleSignInSuccess(account: GoogleSignInAccount) {
        val email = account.email
        email?.let {
            checkIfUserExistsInDatabase(it) { userExists ->
                if (!userExists) {
                    showGoogleSignInUI()
                    // Add this line to call addCategories after showing the UI
                    FirebaseOperations(this).addWordStatsSubcollection(auth.currentUser?.uid ?: "")
                    FirebaseOperations(this).copyWordsToCategories(auth.currentUser?.uid ?: "")

                    //FirebaseOperations(this).copyAnimalDocument(auth.currentUser?.uid ?: "")
                } else {
                    navigateToMainActivity()
                    FirebaseOperations(this).updateUsersScore(auth.currentUser?.uid ?: "")
                }
            }
        }
    }


    private fun showGoogleSignInUI() {
        textViewGoogleNick.visibility = View.VISIBLE
        buttonNickG.visibility = View.VISIBLE
        findViewById<EditText>(R.id.editTextLoginL).visibility = View.GONE
        findViewById<EditText>(R.id.editTextPasswordL).visibility = View.GONE
        findViewById<Button>(R.id.buttonLoginL).visibility = View.GONE
        findViewById<TextView>(R.id.textView_create_now).visibility = View.GONE
        findViewById<TextView>(R.id.textViewForgotPassword).visibility = View.GONE
        findViewById<TextView>(R.id.textViewLoginText).visibility = View.GONE
        findViewById<ImageView>(R.id.imageView3).visibility = View.GONE

    }

    private fun handleConfirmButton() {
        val loginText = textViewGoogleNick.text.toString()
        if (loginText.isNotEmpty()) {
            checkIfLoginIsUnique(loginText) { isUnique ->
                if (isUnique) {
                    saveLoginAndNavigate(loginText)
                } else {
                    showToast("Login is not unique. Choose a different login.")
                }
            }
        } else {
            showToast("Please fill the login field")
        }
    }

    private fun saveLoginAndNavigate(login: String) {
        saveLoginToSharedPreferences(auth.currentUser?.email ?: "")
        saveToUsersCollection(login, auth.currentUser?.email ?: "")
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun preformLogin() {
        val email = findViewById<EditText>(R.id.editTextLoginL)
        val password = findViewById<EditText>(R.id.editTextPasswordL)
        if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
            showToast("Please fill all fields")
            return
        }
        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()

        auth.signInWithEmailAndPassword(inputEmail, inputPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && auth.currentUser?.isEmailVerified == true) {
                    saveLoginToSharedPreferences(inputEmail)
                    navigateToMainActivity()
                    showToast("Success.")
                } else if (auth.currentUser?.isEmailVerified == false) {
                    showToast("You haven't verified your account via mailbox yet.")
                } else {
                    showToast("Authentication failed.")
                }
            }
            .addOnFailureListener {
                showToast("Authentication failed. ${it.localizedMessage}")
            }
    }

    private fun checkIfUserExistsInDatabase(email: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                callback.invoke(!documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                showToast("Error checking user existence: ${exception.localizedMessage}")
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
                showToast("Error checking login uniqueness: ${exception.localizedMessage}")
                callback.invoke(false)
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
            "login" to login,
            "role" to "user",
            "score" to 0
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(auth.currentUser?.uid ?: "")
            .set(userMap)
            .addOnSuccessListener {
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                showToast("Error creating user document: ${e.localizedMessage}")
            }
    }
}