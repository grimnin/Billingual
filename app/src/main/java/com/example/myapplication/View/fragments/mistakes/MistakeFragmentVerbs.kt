package com.example.myapplication.View.fragments.mistakes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.data.IrregularVerb
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class MistakeFragmentVerbs : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var wrongAnswersListWords = mutableListOf<IrregularVerb>()
    private lateinit var adapter: IrregularVerbsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mistake_verbs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebase()
        fetchWrongAnswers()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewVerbs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = IrregularVerbsAdapter(requireContext(), wrongAnswersListWords)
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
            val query = firestore.collection("users").document(userId)
                .collection("stats").document("grammar_stats").collection("grammar")
                .document("irregular_verbs").collection("verbs").whereEqualTo("stats.madeMistake", true)

            query.get().addOnSuccessListener { querySnapshot ->
                handleQuerySnapshot(querySnapshot)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents: ", exception)
            }
        }
    }

    private fun handleQuerySnapshot(querySnapshot: QuerySnapshot) {
        for (document in querySnapshot) {
            val base = document.getString("Basic") ?: ""
            val pastSimple = document.getString("PastSimple") ?: ""
            val pastParticiple = document.getString("PastPerfect") ?: ""
            val meaning = document.getString("pl") ?: ""
            val id = document.getString("id") ?: ""
            val stats = document.get("stats") as? Map<String, Any>
            val madeMistake = stats?.get("madeMistake") as? Boolean ?: false
            val correctAnswers = stats?.get("correctAnswers") as? Long ?: 0
            val wrongAnswers = stats?.get("wrongAnswers") as? Long ?: 0

            val irregularVerb = IrregularVerb(
                base = base,
                pastSimple = pastSimple,
                pastParticiple = pastParticiple,
                meaning = meaning,
                id = id,
                correctAnswers = correctAnswers.toInt(),
                wrongAnswers = wrongAnswers.toInt(),
                madeMistake = madeMistake
            )
            wrongAnswersListWords.add(irregularVerb)
        }

        updateUI()
    }

    private fun updateUI() {
        for (verb in wrongAnswersListWords) {
            Log.d(TAG, "Base: ${verb.base}, Past Simple: ${verb.pastSimple}, Past Participle: ${verb.pastParticiple}, Meaning: ${verb.meaning}, ID: ${verb.id}, Correct Answers: ${verb.correctAnswers}, Wrong Answers: ${verb.wrongAnswers}, Made Mistake: ${verb.madeMistake}")
        }
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "MistakeFragmentVerbs"
    }
}


