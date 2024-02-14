package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMenuBinding
import com.example.myapplication.fragments.mistakes.MistakeFragment
import com.example.myapplication.fragments.quiz.QuizFragment


class MenuFragment : Fragment() {

    private lateinit var binding:FragmentMenuBinding
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMenuBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.QuizImage.setOnClickListener {
            val fragment= QuizFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
        binding.MistakesImage.setOnClickListener {
            val fragment= MistakeFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
    }


}