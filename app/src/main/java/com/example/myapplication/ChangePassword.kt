package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ChangePassword : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var mail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        setConnfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)
        // Odczytaj preferencje językowe


        // Inicjalizacja
        val btnBack: Button = findViewById(R.id.buttonBackToLoginPage)
        val btnReset: Button = findViewById(R.id.buttonChangePassword)
        val inputEmail: EditText = findViewById(R.id.editTextEmailAddressReset)
        auth = FirebaseAuth.getInstance()

        btnBack.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        btnReset.setOnClickListener {
            mail = inputEmail.text.toString()
            if (!mail.isEmpty()) {
                resetPassword()
            } else {
                Toast.makeText(
                    this,
                    "Please provide your email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetPassword() {
        auth.sendPasswordResetEmail(mail)
            .addOnSuccessListener(OnSuccessListener<Void?> {
                Toast.makeText(
                    this,
                    "Reset Password link has been sent to your registered Email",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            })
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
    private fun setConnfiguration(){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
