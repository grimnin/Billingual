package com.example.myapplication.View.fragments.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.myapplication.View.authorization.Login
import com.example.myapplication.R
import com.example.myapplication.View.fragments.MenuFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var changeNickPreference: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Odczytaj preferencje językowe
        val selectedLanguage = loadLanguageFromSharedPreferences()

        // Zastosuj wybraną lokalizację
        setLocale(selectedLanguage)
        setupDarkModeSwitch()
        val darkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Inicjalizacja GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)

        val changeLanguagePreference = findPreference<ListPreference>("reply")

        changeLanguagePreference?.setOnPreferenceChangeListener { preference, newValue ->
            val languageCode = newValue as String
            setLocale(languageCode)
            true
        }

        changeNickPreference = findPreference<EditTextPreference>("login")!!

        // Pobierz aktualny login użytkownika i ustaw go jako summary w preferencji zmiany nicku
        getCurrentUserLogin { login ->
            changeNickPreference.summary = login
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
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Logged out successfully.")
                    redirectToLogin()
                } else {
                    showToast("Failed to logout: ${task.exception?.message}")
                }
            }
            true
        }


        changeNickPreference.setOnBindEditTextListener { editText ->
            // Usuń tekst z pola po jego kliknięciu
            editText.setText("")
        }

        changeNickPreference.setOnPreferenceChangeListener { preference, newValue ->
            val newLogin = newValue.toString()
            if (newLogin.isNotEmpty() && newLogin.length <= 10) {
                // Sprawdź, czy nowy login nie istnieje jeszcze w bazie danych
                checkIfLoginIsUnique(newLogin) { isUnique ->
                    if (isUnique) {
                        // Aktualizuj pole "login" w bazie danych Firestore
                        updateLoginInFirestore(newLogin)
                        // Zaktualizuj summary, aby wyświetlało nowy login
                        changeNickPreference.summary = newLogin
                    } else {
                        showToast("Login already exists. Choose a different login.")
                    }
                }
            } else {
                showToast("Login must not be empty and must be up to 10 characters long.")
            }
            true
        }
    }

    private fun getCurrentUserLogin(callback: (String) -> Unit) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val userRef = firestore.collection("users").document(currentUserUid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val login = document.getString("login") ?: ""
                        callback.invoke(login)
                    } else {
                        showToast("User document does not exist.")
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error getting user document: ${exception.localizedMessage}")
                }
        } else {
            showToast("User is not logged in.")
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateLoginInFirestore(newLogin: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userRef = firestore.collection("users").document(user.uid)
            userRef.update("login", newLogin)
                .addOnSuccessListener {
                    showToast("Login updated successfully.")
                }
                .addOnFailureListener { e ->
                    showToast("Error updating login: ${e.message}")
                }
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
        val menuFragment = com.example.myapplication.View.fragments.MenuFragment()
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, menuFragment)
            .commit()
    }

    private fun redirectToLogin() {
        if (isAdded) {
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }


    private fun loadLanguageFromSharedPreferences(): String {
        return sharedPreferences.getString("language", getString(R.string.default_language_code)) ?: getString(R.string.default_language_code)
    }

    private fun clearSharedPreferences() {
        val selectedLanguage = loadLanguageFromSharedPreferences()
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)
        val editor = sharedPreferences.edit()

        // Usuń wszystkie dane z wyjątkiem tych dotyczących motywu
        editor.remove("language").apply()

        // Przywróć preferencje dotyczące motywu
        editor.putBoolean("darkModeEnabled", isDarkModeEnabled).apply()

        saveLanguageToSharedPreferences(selectedLanguage) // Zachowaj dane dotyczące lokalizacji

        // Przypisz motyw przed ponownym tworzeniem aktywności
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        requireActivity().recreate()
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
    // W SettingsFragment
    private fun setupDarkModeSwitch() {
        val darkModeSwitch = findPreference<SwitchPreferenceCompat>("darkMode")

        darkModeSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val isDarkModeEnabled = newValue as Boolean
            sharedPreferences.edit().putBoolean("darkModeEnabled", isDarkModeEnabled).apply()

            // Ustaw tryb motywu
            if (isDarkModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            true
        }
    }


}

