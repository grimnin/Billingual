package com.example.myapplication.View.fragments.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R

class Exercise2Fragment : Fragment() {

    private lateinit var textViewSentence1: TextView
    private lateinit var textViewSentence2: TextView
    private lateinit var textViewSentence3: TextView
    private lateinit var editTextInput1: EditText
    private lateinit var editTextInput2: EditText
    private lateinit var editTextInput3: EditText
    private lateinit var buttonCheckAnswers: Button
    private lateinit var firebaseOperations: FirebaseOperations

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_exercise2, container, false)
        textViewSentence1 = view.findViewById(R.id.textViewSentence1)
        textViewSentence2 = view.findViewById(R.id.textViewSentence2)
        textViewSentence3 = view.findViewById(R.id.textViewSentence3)
        editTextInput1 = view.findViewById(R.id.editTextInput1)
        editTextInput2 = view.findViewById(R.id.editTextInput2)
        editTextInput3 = view.findViewById(R.id.editTextInput3)
        buttonCheckAnswers = view.findViewById(R.id.buttonCheckAnswers)

        // Obsługa kliknięcia przycisku


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseOperations = FirebaseOperations(requireContext())

        // Pobieranie losowych zdań dla obecnie zalogowanego użytkownika
        firebaseOperations.getRandomSentences { sentences ->
            if (sentences.isNotEmpty()) {
                if (sentences.size >= 3) {
                    textViewSentence1.text = sentences.get(0).zdanie
                    textViewSentence2.text = sentences.get(1).zdanie
                    textViewSentence3.text = sentences.get(2).zdanie
                } else {
                    // Handle case when there are fewer than 3 sentences
                    // For example, display a message indicating insufficient sentences
                }
                buttonCheckAnswers.setOnClickListener {
                    if (validateEditTexts()) {
                        checkAnswers(sentences)
                    }
                }
            } else {
                // Handle case when sentences list is empty
                // For example, display a message indicating no sentences available
            }
        }
    }

    private fun validateEditTexts(): Boolean {
        val inputs = listOf(editTextInput1, editTextInput2, editTextInput3)
        var allFilled = true
        for (input in inputs) {
            if (input.text.trim().isEmpty()) {
                input.error = "Field cannot be empty"
                allFilled = false
            }
        }
        return allFilled
    }

    private fun checkAnswers(sentences: List<Sentence>) {
        val answer1 = editTextInput1.text.toString().trim()
        val answer2 = editTextInput2.text.toString().trim()
        val answer3 = editTextInput3.text.toString().trim()
        val answers = listOf(answer1, answer2, answer3)

        val correctAnswer1 = sentences[0].sentence.trim()
        val correctAnswer2 = sentences[1].sentence.trim()
        val correctAnswer3 = sentences[2].sentence.trim()

        val colorCorrect = ContextCompat.getColor(requireContext(), R.color.correctColor)
        val colorIncorrect = ContextCompat.getColor(requireContext(), R.color.incorrectColor)

        if (answer1.equals(correctAnswer1, ignoreCase = true)) {
            editTextInput1.setBackgroundColor(colorCorrect)
        } else {
            editTextInput1.setBackgroundColor(colorIncorrect)
        }

        if (answer2.equals(correctAnswer2, ignoreCase = true)) {
            editTextInput2.setBackgroundColor(colorCorrect)
        } else {
            editTextInput2.setBackgroundColor(colorIncorrect)
        }

        if (answer3.equals(correctAnswer3, ignoreCase = true)) {
            editTextInput3.setBackgroundColor(colorCorrect)
        } else {
            editTextInput3.setBackgroundColor(colorIncorrect)
        }
        firebaseOperations.updateSentenceStats( sentences, answers)
    }

}


