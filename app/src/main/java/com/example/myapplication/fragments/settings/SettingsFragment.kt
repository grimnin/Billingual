package com.example.myapplication.fragments.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.myapplication.Login
import com.example.myapplication.R
import com.example.myapplication.fragments.MenuFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Odczytaj preferencje językowe
        val selectedLanguage = loadLanguageFromSharedPreferences()

        // Zastosuj wybraną lokalizację
        setLocale(selectedLanguage)

        val changeLanguagePreference = findPreference<ListPreference>("reply")

        changeLanguagePreference?.setOnPreferenceChangeListener { preference, newValue ->
            val languageCode = newValue as String
            setLocale(languageCode)
            true
        }

        val backButtonPreference = findPreference<Preference>("backButton")

        backButtonPreference?.setOnPreferenceClickListener {
            redirectToMenuFragment()
            true
        }

        val logoutPreference = findPreference<Preference>("logout")

        logoutPreference?.setOnPreferenceClickListener {
            clearSharedPreferences()
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()
            redirectToLogin()
            true
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        requireContext().resources.updateConfiguration(configuration, requireContext().resources.displayMetrics)
        requireActivity().recreate()

        // Zapisz wybrany język do SharedPreferences
        saveLanguageToSharedPreferences(languageCode)
    }

    private fun saveLanguageToSharedPreferences(languageCode: String) {
        val editor = sharedPreferences.edit()
        editor.putString("language", languageCode)
        editor.apply()
    }

    private fun redirectToMenuFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val quizFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)
        quizFragment?.let {
            fragmentManager.beginTransaction().remove(it).commit()
        }
        val menuFragment = MenuFragment()
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, menuFragment)
            .commit()
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadLanguageFromSharedPreferences(): String {
        return sharedPreferences.getString("language", getString(R.string.default_language_code)) ?: getString(R.string.default_language_code)
    }

    private fun clearSharedPreferences() {
        val selectedLanguage = loadLanguageFromSharedPreferences()
        val editor = sharedPreferences.edit()
        editor.clear().apply()
        saveLanguageToSharedPreferences(selectedLanguage) // Zachowaj dane dotyczące lokalizacji
    }
}
