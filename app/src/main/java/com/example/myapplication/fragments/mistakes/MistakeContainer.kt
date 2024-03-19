package com.example.myapplication.fragments.mistakes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MistakeContainer : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setConnfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mistake_container)

        // Inicjalizacja BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation_view_mistake)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Words -> showMistakeFragmentWords()
                R.id.Verbs -> showMistakeFragmentVerbs()
            }
            true
        }

        // Wyświetlenie domyślnego fragmentu (np. MistakeFragmentWords)
        showMistakeFragmentWords()
    }

    private fun showMistakeFragmentWords() {
        supportFragmentManager.commit {
            replace<MistakeFragmentWords>(R.id.fragmentContainerViewMistake)
             // Opcjonalnie, jeśli chcesz dodać do cofania transakcji
        }
    }

    private fun showMistakeFragmentVerbs() {
        supportFragmentManager.commit {
            replace<MistakeFragmentVerbs>(R.id.fragmentContainerViewMistake)
             // Opcjonalnie, jeśli chcesz dodać do cofania transakcji
        }
    }

    override fun onBackPressed() {
        // Sprawdź, czy istnieją jakiekolwiek fragmenty na stosie cofania
        if (supportFragmentManager.backStackEntryCount >= 0) {
            // Jeśli stos cofania nie jest pusty, wykonaj standardowe zachowanie przycisku cofania
            super.onBackPressed()
        } else {
            // Jeśli stos cofania jest pusty, przenieś użytkownika do MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
    }

    private fun loadLanguageFromSharedPreferences(): String {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("language", getString(R.string.default_language_code)) ?: getString(R.string.default_language_code)
    }

    private fun setConnfiguration(){
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
