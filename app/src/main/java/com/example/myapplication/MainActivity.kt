package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.myapplication.databinding.MainActivityBinding
import com.example.myapplication.fragments.MenuFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Odczytaj preferencje językowe
        val languageCode = loadLanguageFromSharedPreferences()

        // Zastosuj wybraną lokalizację
        setLocale(languageCode)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)



        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()



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

                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd odczytu z Firestore
                    // W praktyce warto dodać odpowiednie logi lub obsługę błędów
                }

        } else {
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

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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
