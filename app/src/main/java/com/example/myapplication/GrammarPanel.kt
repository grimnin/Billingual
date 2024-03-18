package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.fragments.grammar.Exercise1Fragment
import com.example.myapplication.fragments.grammar.IrregularVerbsFragment
import com.example.myapplication.fragments.grammar.PastTenses
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale

class GrammarPanelActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.grammar_panel)
        setConnfiguration()
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = GrammarPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Irregular Verbs"
                1->"Past Tenses"
                2 -> "Exercises"

                // Dodaj więcej tytułów zakładek, jeśli masz więcej fragmentów
                else -> null
            }

        }.attach()
    }

    private inner class GrammarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> IrregularVerbsFragment()
                1 -> Exercise1Fragment()
                2 -> PastTenses()


                // Dodaj więcej przypadków dla kolejnych fragmentów
                else -> Fragment()
            }
        }
    }

    companion object {
        private const val NUM_PAGES = 3 // Ilość fragmentów
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
}
