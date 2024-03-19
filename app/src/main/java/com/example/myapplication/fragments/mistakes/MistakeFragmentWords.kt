package com.example.myapplication.fragments.mistakes

import WrongAnswersAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.databinding.FragmentMistakeBinding
import com.example.myapplication.fragments.quiz.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MistakeFragmentWords : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentMistakeBinding
    private var wrongAnswersListWords = mutableListOf<WrongAnswerWords>()
    private var selectedPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMistakeBinding.inflate(inflater, container, false)
        viewPager = binding.viewPager
        recyclerView = binding.recyclerView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebase()
        fetchWrongAnswers()



        viewPager.isUserInputEnabled = true
        binding.viewPager.visibility = View.GONE

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedPosition = position
            }
        })
    }

    private fun setupFirebase() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun fetchWrongAnswers() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).collection("stats")
                .document("word_stats").collection("categories").get()
                .addOnSuccessListener { categories ->
                    for (category in categories) {
                        val categoryId = category.id
                        firestore.collection("users").document(userId).collection("stats")
                            .document("word_stats").collection("categories").document(categoryId)
                            .collection("words").whereEqualTo("madeMistake", true).get()
                            .addOnSuccessListener { words ->
                                for ((index, wordDoc) in words.withIndex()) {
                                    val word = wordDoc.toObject(Word::class.java)
                                    wrongAnswersListWords.add(
                                        WrongAnswerWords(
                                            pl = word.pl,
                                            eng = word.eng,
                                            correctCount = word.correctCount,
                                            mistakeCounter = word.mistakeCounter,
                                            total = word.total,
                                            madeMistake = word.madeMistake
                                        )
                                    )
                                }
                                updateRecyclerView()
                            }
                    }
                }
        }
    }

    private fun updateViewPager() {
        if (selectedPosition != -1) {
            val fragmentList = mutableListOf<Fragment>()
            for (wrongAnswer in wrongAnswersListWords) {
                val wordDetailsFragment = WordDetailsFragment()
                val bundle = Bundle().apply {
                    putString("polishTranslation", wrongAnswer.pl)
                    putString("englishTranslation", wrongAnswer.eng)
                    putString("numberOfCorrectAnswers", wrongAnswer.correctCount.toString())
                    putString("numberOfWrongAnswers", wrongAnswer.mistakeCounter.toString())
                }
                wordDetailsFragment.arguments = bundle
                fragmentList.add(wordDetailsFragment)
            }
            val adapter = MistakePagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
            viewPager.adapter = adapter
            viewPager.setCurrentItem(selectedPosition, false)
        }
    }

    private fun updateRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val wrongAnswersAdapter = WrongAnswersAdapter(wrongAnswersListWords, object : WrongAnswersAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                selectedPosition = position
                viewPager.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                updateViewPager()
            }
        })
        recyclerView.adapter = wrongAnswersAdapter
    }
}
