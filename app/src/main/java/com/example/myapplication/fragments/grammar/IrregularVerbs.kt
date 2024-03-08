package com.example.myapplication.fragments.grammar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class IrregularVerbsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IrregularVerbsAdapter
    private var irregularVerbs: List<IrregularVerb> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_irregular_verbs, container, false)

        return view
    }


}
