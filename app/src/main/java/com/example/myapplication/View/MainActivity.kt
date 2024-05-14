package com.example.myapplication.View

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.myapplication.R
import com.example.myapplication.View.authorization.Login
import com.example.myapplication.View.fragments.mistakes.MistakeFragmentWords
import com.example.myapplication.View.fragments.quiz.QuizFragment
import com.example.myapplication.View.fragments.rank.Rank
import com.example.myapplication.View.fragments.settings.SettingsFragment
import com.example.myapplication.utils.ConfigurationHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        ConfigurationHelper.setConfiguration(this) // Ustawianie konfiguracji

        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Sprawdź, czy użytkownik jest zalogowany
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            // Przekieruj do ekranu logowania, jeśli użytkownik nie jest zalogowany
            redirectToLogin()
        }

        if (savedInstanceState == null) {
            // Jeżeli fragment jeszcze nie jest dodany, dodaj go
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Tworzymy instancję MenuFragment
            val menuFragment = com.example.myapplication.View.fragments.MenuFragment()

            // Dodajemy MenuFragment do fragmentContainerView2
            fragmentTransaction.replace(R.id.fragmentContainerView2, menuFragment)
            fragmentTransaction.commit()
        }
    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)

        when (currentFragment) {
            is MistakeFragmentWords,
            is SettingsFragment,
            is Rank,
            is QuizFragment -> {
                // Jeśli bieżący fragment jest jednym z wyżej wymienionych, przejdź do MenuFragment
                val menuFragment = com.example.myapplication.View.fragments.MenuFragment()
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView2, menuFragment)
                    .commit()
            }
            is com.example.myapplication.View.fragments.MenuFragment -> {
                // Jeśli bieżący fragment jest MenuFragment, wyświetl okno dialogowe z pytaniem o zamknięcie aplikacji
                showExitDialog()
            }
            else -> super.onBackPressed()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to exit the app?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}


