package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.myapplication.fragments.MenuFragment
import com.example.myapplication.fragments.mistakes.MistakeFragment
import com.example.myapplication.fragments.quiz.QuizFragment
import com.example.myapplication.fragments.rank.Rank
import com.example.myapplication.fragments.settings.SettingsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        // Odczytaj preferencje językowe
        val languageCode = loadLanguageFromSharedPreferences()

        // Zastosuj wybraną lokalizację
        setLocale(languageCode)
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Ustaw odpowiedni styl w zależności od trybu ciemnego
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
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
            val menuFragment = MenuFragment()

            // Dodajemy MenuFragment do fragmentContainerView2
            fragmentTransaction.replace(R.id.fragmentContainerView2, menuFragment)
            fragmentTransaction.commit()
        }
    }

    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)

        when (currentFragment) {
            is MistakeFragment,
            is SettingsFragment,
            is Rank,
            is QuizFragment -> {
                // Jeśli bieżący fragment jest jednym z wyżej wymienionych, przejdź do MenuFragment
                val menuFragment = MenuFragment()
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView2, menuFragment)
                    .commit()
            }
            is MenuFragment -> {
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
}

