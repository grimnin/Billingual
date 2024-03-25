package com.example.myapplication.fragments.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.myapplication.R

class TestYourself : Fragment() {
    private lateinit var buttonExercise1: Button
    private lateinit var buttonExercise2: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_yourself, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonExercise1 = view.findViewById(R.id.buttonExercise1)
        buttonExercise2 = view.findViewById(R.id.buttonExercise2)

        // Obsługa kliknięcia buttonExercise1
        buttonExercise1.setOnClickListener {
            // Tworzymy instancję fragmentu Exercise1
            buttonExercise1.visibility = View.GONE
            buttonExercise2.visibility = View.GONE
            val exercise1Fragment = Exercise1Fragment()

            // Rozpoczynamy transakcję fragmentu
            parentFragmentManager.commit {
                // Zamieniamy zawartość fragmentContainerView na Exercise1 fragment
                replace(R.id.fragmentContainerViewExercises, exercise1Fragment)
                // Dodajemy transakcję do back stack, aby można było wrócić do poprzedniego stanu
                addToBackStack(null)
            }
        }
        buttonExercise2.setOnClickListener {
            buttonExercise1.visibility = View.GONE
            buttonExercise2.visibility = View.GONE
            val exercise2Fragment = Exercise2Fragment()

            // Rozpoczynamy transakcję fragmentu
            parentFragmentManager.commit {
                // Zamieniamy zawartość fragmentContainerView na Exercise1 fragment
                replace(R.id.fragmentContainerViewExercises, exercise2Fragment)
                // Dodajemy transakcję do back stack, aby można było wrócić do poprzedniego stanu

            }


        }
    }
}
