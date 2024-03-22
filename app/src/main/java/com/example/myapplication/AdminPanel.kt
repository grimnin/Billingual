package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.fragments.administration.AddWordFragment
import com.example.myapplication.fragments.administration.ModifyVerbsFragment
import com.example.myapplication.fragments.administration.UserManagementFragment

import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminPanel : AppCompatActivity() {

    private val usersFragment = UserManagementFragment()
    private val addWordFragment = AddWordFragment()
    private val modifyVerbsFragment=ModifyVerbsFragment()

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_users -> {
                    replaceFragment(usersFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_add_word -> {
                    replaceFragment(addWordFragment)
                    return@OnNavigationItemSelectedListener true
                } R.id.navigation_add_verb -> {
                    replaceFragment(modifyVerbsFragment)
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_panel)
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        replaceFragment(usersFragment) // Startujemy z pierwszym fragmentem
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
