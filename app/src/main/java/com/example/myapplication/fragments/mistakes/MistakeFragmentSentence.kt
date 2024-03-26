package com.example.myapplication.fragments.mistakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.FirebaseOperations
import com.example.myapplication.R

class MistakeFragmentSentence : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MistakeSentenceAdapter
    private lateinit var firebaseOperations: FirebaseOperations

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mistake_sentence, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewSentences)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        firebaseOperations = FirebaseOperations(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MistakeSentenceAdapter(firebaseOperations)
        recyclerView.adapter = adapter
        firebaseOperations.getSentencesWithMistakes { sentences ->
            adapter.setData(sentences)
        }
    }
}
