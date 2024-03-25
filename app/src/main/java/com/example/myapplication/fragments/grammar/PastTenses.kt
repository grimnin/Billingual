package com.example.myapplication.fragments.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class PastTenses : Fragment() {
private lateinit var spinnerExplanation:Spinner
private lateinit var textViewExplanation:TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_past_tenses, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerExplanation= view.findViewById(R.id.spinnerExplanation)
        textViewExplanation= view.findViewById(R.id.textViewExplanation)
        // Definicja listy czasów
        val tenseList = listOf("Past Simple", "Past Continuous", "Present Perfect")

        // Utworzenie adaptera dla Spinnera
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tenseList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExplanation.adapter = adapter

        // Obsługa wyboru elementu z Spinnera
        spinnerExplanation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ustawienie odpowiedniego tekstu w zależności od wybranego czasu
                when (position) {
                    0 -> textViewExplanation.text = resources.getString(R.string.simple_past_explanation)
                    1 -> textViewExplanation.text = resources.getString(R.string.past_continuous_explanation)
                    2 -> textViewExplanation.text = resources.getString(R.string.present_perfect_explanation)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nic nie robimy, gdy nie zostanie nic wybrane
            }
        }
    }
}

