package com.example.myapplication.View.fragments.administration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ModifySentencesFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var spinnerTenses: Spinner
    private lateinit var textViewShowSentence: TextView
    private lateinit var textViewShowZdanie: TextView
    private lateinit var textViewAddSentence: EditText
    private lateinit var textViewAddZdanie: EditText
    private lateinit var buttonAddSentence: Button
    private lateinit var buttonDeleteSentence: Button
    private lateinit var firebaseOperations: FirebaseOperations

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseOperations = FirebaseOperations(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_modify_sentences, container, false)

        spinner = view.findViewById(R.id.spinnerListOfSentences)
        spinnerTenses = view.findViewById(R.id.spinnerListOfTenses)
        textViewShowSentence = view.findViewById(R.id.textViewShowSentence)
        textViewShowZdanie = view.findViewById(R.id.textViewShowZdanie)
        textViewAddSentence = view.findViewById(R.id.editTextAddSentence)
        textViewAddZdanie = view.findViewById(R.id.editTextAddZdanie)
        buttonAddSentence = view.findViewById(R.id.buttonAddSentence)
        buttonDeleteSentence = view.findViewById(R.id.buttonDeleteSentence)

        // Load spinner items
        loadSpinnerItems()

        // Set up spinner listener
        setupSpinnerListener()

        // Set up delete button listener
        setupDeleteButtonListener()

        // Set up add sentence button listener
        setupAddSentenceButtonListener()

        return view
    }

    private fun setupAddSentenceButtonListener() {
        buttonAddSentence.setOnClickListener {
            val newSentence = textViewAddSentence.text.toString().trim()
            val newZdanie = textViewAddZdanie.text.toString().trim()
            val tense=spinnerTenses.selectedItem.toString()
            val newSentenceId = "sentence${spinner.adapter.count + 1}"
            // Check if the fields are not empty
            if (newSentence.isNotEmpty() && newZdanie.isNotEmpty()) {
                Log.d("Para","$newSentence,$newZdanie,$tense,$newSentenceId")
                firebaseOperations.addSentenceForAllUsers(newSentence, newZdanie,tense, newSentenceId)
                textViewAddSentence.text.clear()
                textViewAddZdanie.text.clear()
                loadSpinnerItems()

            }
        }
    }


    private fun loadSpinnerItems() {
        // Pobierz aktualnego użytkownika
        val user = FirebaseAuth.getInstance().currentUser

        // Sprawdź, czy użytkownik jest zalogowany
        user?.let { currentUser ->
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            // Pobierz identyfikatory zdań
            val sentencesCollectionRef = db.collection("users")
                .document(userId)
                .collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")
                .collection("sentences")

            sentencesCollectionRef.get()
                .addOnSuccessListener { documents ->
                    // Lista identyfikatorów zdań
                    val sentenceIds = mutableListOf<String>()
                    for (document in documents) {
                        val sentenceId = document.id
                        sentenceIds.add(sentenceId)
                    }

                    // Dodaj opcję "Dodaj zdanie"
                    sentenceIds.add("Add sentence")

                    // Ustaw spinner dla zdań
                    setupSpinner(sentenceIds)
                }
                .addOnFailureListener { exception ->
                    Log.e("ModifySentencesFragment", "Error loading sentences", exception)
                }

            // Pobierz czasy gramatyczne
            val tensesCollectionRef = db.collection("users")
                .document(userId)
                .collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")

            tensesCollectionRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    // Mapa czasów gramatycznych
                    val tensesMap = documentSnapshot.data

                    // Lista czasów gramatycznych
                    val tensesList = mutableListOf<String>()

                    // Iteruj przez wartości mapy
                    tensesMap?.forEach { (_, value) ->
                        if (value is String) {
                            tensesList.add(value)
                        } else if (value is Map<*, *>) {
                            value.values.forEach { innerValue ->
                                if (innerValue is String) {
                                    tensesList.add(innerValue)
                                }
                            }
                        }
                    }

                    // Ustaw spinner dla czasów gramatycznych
                    setupSpinnerTenses(tensesList)
                }
                .addOnFailureListener { exception ->
                    Log.e("ModifySentencesFragment", "Error loading tenses", exception)
                }
        }
    }











    private fun setupSpinner(sentenceIds: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sentenceIds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupSpinnerTenses(tensesList: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tensesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenses.adapter = adapter
    }

    private fun setupSpinnerListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = spinner.selectedItem as String
                if (selectedItem == "Add sentence") {
                    buttonAddSentence.visibility = View.VISIBLE
                    buttonDeleteSentence.visibility = View.GONE
                    textViewShowSentence.visibility = View.GONE
                    textViewShowZdanie.visibility = View.GONE
                    textViewShowSentence.text = ""
                    textViewShowZdanie.text = ""
                    textViewAddSentence.visibility = View.VISIBLE
                    textViewAddZdanie.visibility = View.VISIBLE
                    spinnerTenses.visibility = View.VISIBLE
                } else {
                    buttonAddSentence.visibility = View.GONE
                    buttonDeleteSentence.visibility = View.VISIBLE
                    textViewShowSentence.visibility = View.VISIBLE
                    textViewShowZdanie.visibility = View.VISIBLE
                    textViewAddSentence.visibility = View.GONE
                    textViewAddZdanie.visibility = View.GONE
                    spinnerTenses.visibility = View.GONE
                    updateTextFields(selectedItem)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected
            }
        }
    }

    private fun setupDeleteButtonListener() {
        buttonDeleteSentence.setOnClickListener {
            val selectedItem = spinner.selectedItem as String
            if (selectedItem != "Add sentence") {
                firebaseOperations.deleteSentenceForAllUsers(selectedItem)
                loadSpinnerItems()
            }
        }
    }

    private fun updateTextFields(selectedSentenceId: String) {
        // Fetch data from Firestore based on the selected sentence ID
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            val sentenceRef = db.collection("users")
                .document(userId)
                .collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")
                .collection("sentences")
                .document(selectedSentenceId)

            sentenceRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val sentence = documentSnapshot.getString("sentence")
                        val zdanie = documentSnapshot.getString("zdanie")
                        textViewShowSentence.text = sentence
                        textViewShowZdanie.text = zdanie
                    } else {
// Handle when the document does not exist
                        Log.e("ModifySentencesFragment", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
// Handle errors
                    Log.e("ModifySentencesFragment", "Error fetching sentence details", exception)
                }
        }
    }
}
