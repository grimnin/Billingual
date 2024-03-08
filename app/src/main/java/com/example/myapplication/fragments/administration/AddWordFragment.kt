package com.example.myapplication.fragments.administration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject

class AddWordFragment : Fragment() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextNewCategory: EditText
    private lateinit var editTextWrongAnswer1: EditText
    private lateinit var editTextWrongAnswer2: EditText
    private lateinit var editTextWrongAnswer3: EditText
    private lateinit var editTextCorrectAnswer: EditText
    private lateinit var editTextPl: EditText
    private lateinit var buttonAddWord: Button
    private val storageRef = FirebaseStorage.getInstance().reference
    private val jsonFilePath = "betterAnswers.json"

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_word, container, false)

        // Initialize views
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        editTextNewCategory = view.findViewById(R.id.editTextNewCategory)
        editTextWrongAnswer1 = view.findViewById(R.id.editTextWrongAnswer1)
        editTextWrongAnswer2 = view.findViewById(R.id.editTextWrongAnswer2)
        editTextWrongAnswer3 = view.findViewById(R.id.editTextWrongAnswer3)
        editTextCorrectAnswer = view.findViewById(R.id.editTextCorrectAnswer)
        editTextPl = view.findViewById(R.id.editTextPl)
        buttonAddWord = view.findViewById(R.id.buttonAddWord)

        setupSpinner()
        setupButton()

        return view
    }

    private fun setupSpinner() {
        // Get categories list from Firestore database
        val categories = mutableListOf<String>()

        firestore.collection("words")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("name")
                    categoryName?.let { categories.add(it) }
                }

                // Add "Add new category" option
                categories.add("Add new category")

                // Set adapter for Spinner
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Listen for category selection
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Add new category") {
                    // Show EditText field for new category
                    editTextNewCategory.visibility = View.VISIBLE
                } else {
                    // Hide EditText field for new category
                    editTextNewCategory.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing when no item is selected
            }
        }
    }

    private fun setupButton() {
        buttonAddWord.setOnClickListener {
            addWordToFirestore()
        }
    }

    private fun addWordToFirestore() {
        val category = if (spinnerCategory.selectedItem.toString() == "Add new category") {
            editTextNewCategory.text.toString().trim()
        } else {
            spinnerCategory.selectedItem.toString()
        }

        val eng = editTextCorrectAnswer.text.toString().trim()
        val pl = editTextPl.text.toString().trim() // Assign value from editTextPl to 'pl'
        val wrongAnswer1 = editTextWrongAnswer1.text.toString().trim()
        val wrongAnswer2 = editTextWrongAnswer2.text.toString().trim()
        val wrongAnswer3 = editTextWrongAnswer3.text.toString().trim()

        if (category.isNotEmpty() && eng.isNotEmpty() && pl.isNotEmpty()) {
            val wordData = hashMapOf(
                "eng" to eng,
                "pl" to pl, // Assign value from editTextPl to 'pl'
                "total" to 0,
                "mistakeCounter" to 0,
                "correctCount" to 0,
                "madeMistake" to false
            )

            // Get current document count in the collection
            firestore.collection("words").document(category).collection("words")
                .get()
                .addOnSuccessListener { documents ->
                    val wordCount = documents.size() + 1
                    // Create ID for the new document
                    val newWordId = "word$wordCount" // Now the ID will be in the format "wordX"

                    // Add word to the "words" collection in the appropriate category
                    firestore.collection("words").document(category).collection("words")
                        .document(newWordId)
                        .set(wordData)
                        .addOnSuccessListener {
                            // Update JSON in Firebase Storage
                            if (spinnerCategory.selectedItem.toString() == "Add new category") {
                                // Create a new category in JSON with the new word
                                val newCategory = editTextNewCategory.text.toString().trim()
                                val newWordJSON = JSONObject().apply {
                                    put("pl", pl)
                                    put("answers", JSONArray().apply {
                                        put(wrongAnswer1)
                                        put(wrongAnswer2)
                                        put(wrongAnswer3)
                                        put(eng)
                                    })
                                    put("correctAnswer", eng)
                                }
                                updateJsonInStorage(newCategory, newWordJSON)
                            } else {
                                // Add new word to the existing category
                                val newWordJSON = JSONObject().apply {
                                    put("pl", pl)
                                    put("answers", JSONArray().apply {
                                        put(wrongAnswer1)
                                        put(wrongAnswer2)
                                        put(wrongAnswer3)
                                        put(eng)
                                    })
                                    put("correctAnswer", eng)
                                }
                                updateJsonInStorage(category, newWordJSON)
                            }

                            // Update users collection for each user
                            updateUsersCollection(category, eng,pl, newWordId)

                            // Notification of success
                            Toast.makeText(requireContext(), "Word added successfully!", Toast.LENGTH_SHORT).show()
                            // Clear EditText fields
                            editTextNewCategory.text.clear()
                            editTextCorrectAnswer.text.clear()
                            editTextPl.text.clear()
                            editTextWrongAnswer1.text.clear()
                            editTextWrongAnswer2.text.clear()
                            editTextWrongAnswer3.text.clear()
                        }
                        .addOnFailureListener { e ->
                            // Handle error
                            Toast.makeText(requireContext(), "Error adding word: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Toast.makeText(requireContext(), "Error getting document count: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // If a new category is being created, add a "name" field to the document
            if (spinnerCategory.selectedItem.toString() == "Add new category") {
                val categoryName = editTextNewCategory.text.toString().trim()
                val categoryData = hashMapOf(
                    "name" to categoryName
                )

                firestore.collection("words").document(category)
                    .set(categoryData)
                    .addOnSuccessListener {
                        // Notification of success
                        Toast.makeText(requireContext(), "Category added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle error
                        Toast.makeText(requireContext(), "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            // Message about missing required data
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateJsonInStorage(category: String, newWord: JSONObject) {
        // Get current JSON file from Firebase Storage
        val storageFileRef = storageRef.child(jsonFilePath)
        storageFileRef.getBytes(1024 * 1024) // maximum size in bytes
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes)

                // Parse existing JSON
                val jsonObject = JSONObject(jsonString)

                // Add new word to the appropriate category
                val categoriesObject = jsonObject.getJSONObject("categories")
                if (categoriesObject.has(category)) {
                    categoriesObject.getJSONObject(category).getJSONArray("words").put(newWord)
                } else {
                    // Create a new category and add the new word
                    val newCategoryObject = JSONObject().apply {
                        put("name", category)
                        put("words", JSONArray().apply {
                            put(newWord)
                        })
                    }
                    categoriesObject.put(category, newCategoryObject)
                }

                // Upload the updated JSON file back to Firebase Storage
                uploadJsonToStorage(jsonObject.toString().toByteArray())
            }
            .addOnFailureListener { exception ->
                // Handle error fetching JSON file from Firebase Storage
                Toast.makeText(requireContext(), "Error fetching JSON from storage: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadJsonToStorage(jsonBytes: ByteArray) {
        // Upload the updated JSON file back to Firebase Storage
        val storageFileRef = storageRef.child(jsonFilePath)
        storageFileRef.putBytes(jsonBytes)
            .addOnSuccessListener {
                // Notification of success
                Toast.makeText(requireContext(), "JSON updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle error uploading JSON file to Firebase Storage
                Toast.makeText(requireContext(), "Error uploading JSON to storage: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUsersCollection(category: String, word: String,pl:String, newWordId: String) {
        // Update documents in the "users" collection for each user
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.id
                    val userStatsRef = firestore.collection("users").document(userId)
                        .collection("stats").document("word_stats")
                    val userCategoriesRef = userStatsRef.collection("categories")

                    // Check if stats/word_stats document exists for the user
                    userStatsRef.get()
                        .addOnSuccessListener { userStatsDocument ->
                            if (userStatsDocument.exists()) {
                                // If stats/word_stats document exists, get the categories subcollection
                                userCategoriesRef.document(category)
                                    .set(hashMapOf("name" to category))
                                    .addOnSuccessListener {
                                        // After creating/updating category document, add word
                                        val newWordData = hashMapOf(
                                            "eng" to word,
                                            "pl" to pl, // Assign value from editTextPl to 'pl'
                                            "total" to 0,
                                            "mistakeCounter" to 0,
                                            "correctCount" to 0,
                                            "madeMistake" to false
                                        )
                                        val newWordDocRef = userCategoriesRef.document(category)
                                            .collection("words").document(newWordId)
                                        newWordDocRef.set(newWordData)
                                            .addOnSuccessListener {
                                                // Notification of success
                                                Toast.makeText(requireContext(), "User document updated successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle error adding new word in category
                                                Toast.makeText(requireContext(), "Error adding word in category: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle error creating/updating category document
                                        Toast.makeText(requireContext(), "Error creating/updating category: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // If stats/word_stats document doesn't exist, create it
                                val newStatsData = hashMapOf<String, Any>("categories" to hashMapOf<String, Any>())
                                userStatsRef.set(newStatsData)
                                    .addOnSuccessListener {
                                        // After creating stats/word_stats document, create category and add word
                                        userCategoriesRef.document(category)
                                            .set(hashMapOf("name" to category))
                                            .addOnSuccessListener {
                                                // After creating category document, add word
                                                val newWordData = hashMapOf(
                                                    "eng" to word,
                                                    "pl" to word, // Assign value from editTextPl to 'pl'
                                                    "total" to 0,
                                                    "mistakeCounter" to 0,
                                                    "correctCount" to 0,
                                                    "madeMistake" to false
                                                )
                                                val newWordDocRef = userCategoriesRef.document(category)
                                                    .collection("words").document(newWordId)
                                                newWordDocRef.set(newWordData)
                                                    .addOnSuccessListener {
                                                        // Notification of success
                                                        Toast.makeText(requireContext(), "User document updated successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Handle error adding new word in category
                                                        Toast.makeText(requireContext(), "Error adding word in category: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle error creating category document
                                                Toast.makeText(requireContext(), "Error creating category: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle error creating stats/word_stats document
                                        Toast.makeText(requireContext(), "Error creating stats document: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle error fetching stats/word_stats document
                            Toast.makeText(requireContext(), "Error fetching stats document: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle error fetching documents from "users" collection
                Toast.makeText(requireContext(), "Error fetching user documents: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}