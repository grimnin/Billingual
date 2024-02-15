package com.example.myapplication.fragments.mistakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWordDetailsBinding

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
        binding.btnDelete.setOnClickListener {

        }

    }
}
