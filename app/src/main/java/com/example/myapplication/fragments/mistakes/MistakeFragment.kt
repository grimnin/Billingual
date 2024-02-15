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
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMistakeBinding
import com.example.myapplication.fragments.MenuFragment
import com.example.myapplication.fragments.quiz.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MistakeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentMistakeBinding
    private var wrongAnswersList = mutableListOf<WrongAnswer>()

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

        binding.buttonBackToMenu.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager

            // Usuń fragment QuizFragment z kontenera
            val quizFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)
            quizFragment?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }

            // Wyświetl MenuFragment w kontenerze
            val menuFragment = MenuFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, menuFragment)
                .commit()
        }

        // Dodaj obsługę przesuwania palcem nad ViewPagerem
        viewPager.isUserInputEnabled = true // Włącz obsługę przesuwania palcem
        binding.viewPager.visibility = View.GONE // Ukryj ViewPager na początku
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
                                for (wordDoc in words) {
                                    val word = wordDoc.toObject(Word::class.java)
                                    wrongAnswersList.add(
                                        WrongAnswer(
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
                                updateViewPager()
                            }
                    }
                }
        }
    }

    private fun updateViewPagerVisibility(showViewPager: Boolean) {
        if (showViewPager) {
            binding.recyclerView.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
        }
    }

    private fun updateRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val wrongAnswersAdapter = WrongAnswersAdapter(wrongAnswersList, object : WrongAnswersAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                viewPager.currentItem = position
                updateViewPagerVisibility(true) // Po kliknięciu w pozycję RecyclerView, pokaż ViewPager
            }
        })
        recyclerView.adapter = wrongAnswersAdapter
    }


    private fun updateViewPager() {
        val fragmentList = wrongAnswersList.map { word ->
            val wordDetailsFragment = WordDetailsFragment()
            val bundle = Bundle().apply {
                putString("polishTranslation", word.pl)
                putString("englishTranslation", word.eng)
                putString("numberOfCorrectAnswers",word.correctCount.toString())
                putString("numberOfWrongAnswers",word.mistakeCounter.toString()) // poprawione
            }
            wordDetailsFragment.arguments = bundle
            wordDetailsFragment
        }
        val adapter = MistakePagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                recyclerView.scrollToPosition(position)
            }
        })
    }
    fun deleteItemAndUpdate(position: Int) {
        wrongAnswersList.removeAt(position)
        recyclerView.adapter?.notifyItemRemoved(position)
        recyclerView.adapter?.notifyItemRangeChanged(position, wrongAnswersList.size)
    }

    fun setCurrentItem(position: Int) {
        viewPager.currentItem = position
        updateViewPagerVisibility(true)
    }

}


