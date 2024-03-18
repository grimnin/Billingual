package com.example.myapplication.fragments.grammar

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.FirebaseOperations
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class Exercise1Fragment : Fragment() {
    private lateinit var firebaseOperations: FirebaseOperations
    private lateinit var auth: FirebaseAuth
    private lateinit var tableLayout: TableLayout
    private lateinit var buttonSubmit: Button
    private var score = 0
    private lateinit var verbsList: List<IrregularVerb>
    private var isSubmitEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise1, container, false)
        firebaseOperations = FirebaseOperations(requireContext())
        auth = FirebaseAuth.getInstance()

        tableLayout = view.findViewById(R.id.tableLayout)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener { checkAnswers() }

        auth.currentUser?.uid?.let {
            firebaseOperations.getRandomVerbs(it) { verbs ->
                verbsList = verbs
                verbs.forEach { verb ->
                    addRowToTableLayout(tableLayout, verb)
                }
            }
        }

        return view
    }

    private fun addRowToTableLayout(tableLayout: TableLayout, verb: IrregularVerb) {
        val inflater = LayoutInflater.from(requireContext())
        val rowView = inflater.inflate(R.layout.item_exercise1, tableLayout, false)
        rowView.findViewById<TextView>(R.id.textViewPL).text = verb.meaning
        rowView.findViewById<EditText>(R.id.editTextBase).setText("")
        rowView.findViewById<EditText>(R.id.editTextPastSimple).setText("")
        rowView.findViewById<EditText>(R.id.editTextPastPerfect).setText("")
        tableLayout.addView(rowView)
    }

    private fun checkAnswers() {
        isSubmitEnabled = false
        buttonSubmit.isEnabled = false
        var allFieldsFilled = true

        for ((index, verb) in verbsList.withIndex()) {
            val row = tableLayout.getChildAt(index)
            if (row is ViewGroup) {
                val baseEditText = row.findViewById<EditText>(R.id.editTextBase)
                val pastSimpleEditText = row.findViewById<EditText>(R.id.editTextPastSimple)
                val pastPerfectEditText = row.findViewById<EditText>(R.id.editTextPastPerfect)

                val base = baseEditText.text.toString().trim()
                val pastSimple = pastSimpleEditText.text.toString().trim()
                val pastPerfect = pastPerfectEditText.text.toString().trim()

                if (base.isEmpty() || pastSimple.isEmpty() || pastPerfect.isEmpty()) {
                    allFieldsFilled = false
                    Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                    break
                }

                val isBaseCorrect = verb.base.equals(base, ignoreCase = true)
                val isPastSimpleCorrect = verb.pastSimple.equals(pastSimple, ignoreCase = true)
                val isPastPerfectCorrect = verb.pastParticiple.equals(pastPerfect, ignoreCase = true)

                // Ustawienie koloru tekstu dla pól EditText w zależności od poprawności odpowiedzi
                baseEditText.setTextColor(ContextCompat.getColor(requireContext(), if (isBaseCorrect) R.color.correctColor else R.color.incorrectColor))
                pastSimpleEditText.setTextColor(ContextCompat.getColor(requireContext(), if (isPastSimpleCorrect) R.color.correctColor else R.color.incorrectColor))
                pastPerfectEditText.setTextColor(ContextCompat.getColor(requireContext(), if (isPastPerfectCorrect) R.color.correctColor else R.color.incorrectColor))

                // Aktualizacja statystyk czasownika w Firebase Firestore
                firebaseOperations.updateVerbStats(auth.currentUser?.uid ?: "", verb.id, isBaseCorrect && isPastSimpleCorrect && isPastPerfectCorrect)

                val handler = Handler()
                handler.postDelayed({
                    baseEditText.setText("")
                    pastSimpleEditText.setText("")
                    pastPerfectEditText.setText("")

                    // Włącz przycisk submit ponownie
                    isSubmitEnabled = true
                    buttonSubmit.isEnabled = true
                    baseEditText.setTextColor(ContextCompat.getColor(requireContext(),  R.color.black ))
                    pastSimpleEditText.setTextColor(ContextCompat.getColor(requireContext(),  R.color.black ))
                    pastPerfectEditText.setTextColor(ContextCompat.getColor(requireContext(),  R.color.black ))
                }, 2000)

                if (isBaseCorrect && isPastSimpleCorrect && isPastPerfectCorrect) {
                    score += 2
                }
            }
        }

        if (allFieldsFilled) {
            Log.d("ALL", "$allFieldsFilled and score is $score")
            firebaseOperations.updateUserScore(auth.currentUser?.uid ?: "", score)
            score = 0
        }
    }



}
