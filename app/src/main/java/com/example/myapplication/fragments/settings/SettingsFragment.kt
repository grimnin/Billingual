package com.example.myapplication.fragments.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.myapplication.Login
import com.example.myapplication.R
import com.example.myapplication.fragments.MenuFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Initialize FirebaseAuth, FirebaseFirestore, and SharedPreferences
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Register the preference change listener


        // Find the "Change login" preference
        val changeLoginPreference = findPreference<EditTextPreference>("login")
        changeLoginPreference?.text = ""

        // Set the summary of "Change login" preference to the user's login
        updateLoginSummary(changeLoginPreference)

        // Set a listener to update the summary if the login changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                updateLoginSummary(changeLoginPreference)
            }
        }

        // Set a listener for changes in the "Change login" preference
        changeLoginPreference?.setOnPreferenceChangeListener { preference, newValue ->
            val newLogin = newValue.toString()
            if (isLoginUnique(newLogin)) {
                updateLoginInFirestore(newLogin)
                true // Return true to indicate that the preference change should be saved
            } else {
                false // Return false to indicate that the preference change should be discarded
            }
        }

        // Find the "Back" button preference
        val backButtonPreference = findPreference<Preference>("backButton")

        // Set a listener for the "Back" button
        backButtonPreference?.setOnPreferenceClickListener {
            redirectToMenuFragment()
            true // Return true to indicate that the click was handled
        }

        // Find the "Log out" preference
        val logoutPreference = findPreference<Preference>("logout")

        // Set a listener for the "Log out" preference
        logoutPreference?.setOnPreferenceClickListener {
            clearSharedPreferences()
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()
            redirectToLogin()
            true // Return true to indicate that the click was handled
        }
    }



    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        requireContext().resources.updateConfiguration(configuration, requireContext().resources.displayMetrics)
        requireActivity().recreate()
    }

    private fun updateLoginSummary(preference: EditTextPreference?) {
        val currentUser: FirebaseUser? = auth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            // Fetch the login from Firestore and set it as the summary
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val login = document.getString("login")
                        preference?.summary = login
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure to fetch login from Firestore
                }
        }
    }

    private fun isLoginUnique(newLogin: String): Boolean {
        var isUnique = true

        // Check if the new login length is less than 12 characters
        val isLengthValid = newLogin.length < 12

        // If the length is greater than or equal to 12, set the isUnique flag to false
        if (!isLengthValid) {
            isUnique = false
        } else {
            // If the length is valid, check if the login already exists in the users collection
            firestore.collection("users")
                .whereEqualTo("login", newLogin)
                .get()
                .addOnSuccessListener { documents ->
                    // If documents exist, set the isUnique flag to false
                    if (!documents.isEmpty) {
                        isUnique = false
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle Firestore query failure
                    // For demonstration purposes, assume the login is not unique in case of an error
                    isUnique = false
                }
        }

        return isUnique
    }

    private fun updateLoginInFirestore(newLogin: String) {
        val currentUser: FirebaseUser? = auth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid)
                .update("login", newLogin)
                .addOnSuccessListener {
                    // Update successful
                    // Update the summary with the new login
                    updateLoginSummary(findPreference("login"))
                }
                .addOnFailureListener { exception ->
                    // Handle failure to update login in Firestore
                }
        }
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

    private fun clearSharedPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }
}
