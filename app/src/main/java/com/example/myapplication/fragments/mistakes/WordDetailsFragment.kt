package com.example.myapplication.fragments.mistakes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWordDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WordDetailsFragment : Fragment() {

    private lateinit var binding: FragmentWordDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val polishTranslation = arguments?.getString("polishTranslation")
        val englishTranslation = arguments?.getString("englishTranslation")
        val corretCount=arguments?.getString("numberOfCorrectAnswers")
        val incorretCount=arguments?.getString("numberOfWrongAnswers")


        binding.tvPolishTranslation.text = polishTranslation
        binding.tvEnglishTranslation.text = englishTranslation
        binding.tvCorrectCount.text=corretCount
        binding.tvWrongCount.text=incorretCount
        binding.btnBack.setOnClickListener {
            refreshPage()
        }
        binding.btnDelete.setOnClickListener {

            updateMadeMistakeValue(false)

        }

    }
    private fun updateMadeMistakeValue(newValue: Boolean) {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val userId = user.uid
            val englishTranslation = arguments?.getString("englishTranslation")
            Log.d("ENG","$englishTranslation")

            englishTranslation?.let { engTranslation ->
                firestore.collection("users").document(userId)
                    .collection("stats").document("word_stats")
                    .collection("categories")
                    .get()
                    .addOnSuccessListener { categories ->
                        categories.forEach { category ->
                            val categoryId = category.id
                            firestore.collection("users").document(userId)
                                .collection("stats").document("word_stats")
                                .collection("categories").document(categoryId)
                                .collection("words")
                                .whereEqualTo("eng", engTranslation) // Pobierz dokumenty, których pole "eng" nie jest równe "englishTranslation"
                                .get()
                                .addOnSuccessListener { words ->
                                    words.forEach { word ->
                                        word.reference.update("madeMistake", newValue)
                                            .addOnSuccessListener {
                                                refreshPage()
                                            }
                                            .addOnFailureListener { e ->
                                                // Nie udało się zaktualizować wartości
                                                // Tutaj możesz dodać obsługę błędów
                                            }
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Obsługa błędów podczas pobierania dokumentów
                    }
            }
        }
    }
private fun refreshPage(){
    val fragmentManager = requireActivity().supportFragmentManager

    // Usuń fragment QuizFragment z kontenera
    val quizFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)
    quizFragment?.let {
        fragmentManager.beginTransaction().remove(it).commit()
    }

    // Wyświetl MenuFragment w kontenerze
    val mistakeFragment = MistakeFragment()
    fragmentManager.beginTransaction()
        .replace(R.id.fragmentContainerView2, mistakeFragment)
        .commit()
}


}
