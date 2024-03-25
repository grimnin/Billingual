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
import com.example.myapplication.fragments.grammar.IrregularVerbsFragment
import com.example.myapplication.fragments.grammar.PastTenses
import com.example.myapplication.fragments.grammar.TestYourself
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class GrammarPanelActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setConnfiguration()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grammar_panel)

        viewPager = findViewById(R.id.viewPager)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val adapter = GrammarPagerAdapter(this)
        viewPager.adapter = adapter

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_irregular_verbs -> viewPager.currentItem = 0
                R.id.nav_past_tenses -> viewPager.currentItem = 1
                R.id.nav_exercises -> viewPager.currentItem = 2
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }

    private inner class GrammarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> IrregularVerbsFragment()
                1 -> PastTenses()
                2 -> TestYourself()
                else -> Fragment()
            }
        }
    }

    companion object {
        private const val NUM_PAGES = 3 // Ilość fragmentów
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
