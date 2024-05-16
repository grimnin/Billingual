package com.example.myapplication.View.fragments.mistakes

import WrongAnswerWordsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.data.WrongAnswerWords
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MistakeFragmentWords : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var wrongAnswersListWords = mutableListOf<WrongAnswerWords>()
    private lateinit var adapter: WrongAnswerWordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mistake_words, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebase()
        fetchWrongAnswers()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewWords)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = WrongAnswerWordsAdapter(requireContext(), wrongAnswersListWords)
        recyclerView.adapter = adapter
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
                                    val word = wordDoc.toObject(WrongAnswerWords::class.java)
                                    wrongAnswersListWords.add(word)
                                }
                                updateUI()
                            }
                    }
                }
        }
    }

    private fun updateUI() {
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "MistakeFragmentWords"
    }
}
