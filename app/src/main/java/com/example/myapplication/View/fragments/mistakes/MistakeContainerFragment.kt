package com.example.myapplication.View.fragments.mistakes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MistakeContainerFragment : Fragment() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicjalizacja BottomNavigationView
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)
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
        childFragmentManager.commit {
            replace<MistakeFragmentWords>(R.id.fragmentContainerViewMistake)
            addToBackStack(null) // Opcjonalnie, jeśli chcesz dodać do cofania transakcji
        }
    }

    private fun showMistakeFragmentVerbs() {
        childFragmentManager.commit {
            replace<MistakeFragmentVerbs>(R.id.fragmentContainerViewMistake)
            addToBackStack(null) // Opcjonalnie, jeśli chcesz dodać do cofania transakcji
        }
    }
}
