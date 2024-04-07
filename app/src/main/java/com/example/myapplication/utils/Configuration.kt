package com.example.myapplication.utils
import android.content.Context
import android.content.res.Configuration
import com.example.myapplication.R
import java.util.Locale

class ConfigurationHelper {
    companion object {
        fun setConfiguration(context: Context) {
            setLocale(context)
            setDarkMode(context)
        }

        private fun setLocale(context: Context) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("language", context.getString(R.string.default_language_code))
                ?: context.getString(R.string.default_language_code)
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val configuration = Configuration()
            configuration.setLocale(locale)
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }

        private fun setDarkMode(context: Context) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)
            if (isDarkModeEnabled) {
                context.setTheme(R.style.AppTheme_Dark)
            } else {
                context.setTheme(R.style.AppTheme_Light)
            }
        }
    }
}
