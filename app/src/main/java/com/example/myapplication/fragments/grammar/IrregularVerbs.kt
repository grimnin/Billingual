package com.example.myapplication.fragments.grammar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONException
import org.json.JSONObject

class IrregularVerbsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IrregularVerbsAdapter
    private lateinit var btnExercise1: Button
    private var irregularVerbs: MutableList<IrregularVerb> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_irregular_verbs, container, false)
        recyclerView = view.findViewById(R.id.recycleVerbs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadIrregularVerbsFromJson()


        return view
    }

    private fun loadIrregularVerbsFromJson() {
        val storageRef = Firebase.storage.reference
        val jsonRef = storageRef.child("verbs.json")

        val MAX_DOWNLOAD_SIZE: Long = 1024 * 1024 // 1 MB max download size
        jsonRef.getBytes(MAX_DOWNLOAD_SIZE).addOnSuccessListener { bytes ->
            val jsonString = String(bytes)
            parseJson(jsonString)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading JSON file", exception)
        }
    }

    private fun parseJson(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("verbs")

            for (i in 0 until jsonArray.length()) {
                val verbObject = jsonArray.getJSONObject(i)
                val base = verbObject.getString("Base")
                val pastSimple = verbObject.getString("Past-Simple")
                val pastParticiple = verbObject.getString("Past-Participle")
                val meaning = verbObject.getString("PL")

                val irregularVerb = IrregularVerb(base, pastSimple, pastParticiple, meaning,"",0,0,false)
                irregularVerbs.add(irregularVerb)
            }

            adapter = IrregularVerbsAdapter(irregularVerbs)
            recyclerView.adapter = adapter

        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON", e)
        }
    }

    companion object {
        private const val TAG = "IrregularVerbsFragment"
    }
}



