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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AddWordFragment : Fragment() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCategoryDelete: Spinner
    private lateinit var spinnerWord: Spinner
    private lateinit var editTextNewCategory: EditText
    private lateinit var editTextWrongAnswer1: EditText
    private lateinit var editTextWrongAnswer2: EditText
    private lateinit var editTextWrongAnswer3: EditText
    private lateinit var editTextCorrectAnswer: EditText
    private lateinit var editTextPl: EditText
    private lateinit var buttonAddWord: Button
    private lateinit var buttonDeleteWord: Button
    private val storageRef = FirebaseStorage.getInstance().reference
    private val jsonFilePath = "betterAnswers.json"
    private lateinit var firebaseOperations: FirebaseOperations
    private var selectedWord=""



    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseOperations = FirebaseOperations(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_word, container, false)

        // Initialize views
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        spinnerCategoryDelete = view.findViewById(R.id.spinnerSelectCategoryToDelete)
        spinnerWord = view.findViewById(R.id.spinnerSelectWordToDelete)
        editTextNewCategory = view.findViewById(R.id.editTextNewCategory)
        editTextWrongAnswer1 = view.findViewById(R.id.editTextWrongAnswer1)
        editTextWrongAnswer2 = view.findViewById(R.id.editTextWrongAnswer2)
        editTextWrongAnswer3 = view.findViewById(R.id.editTextWrongAnswer3)
        editTextCorrectAnswer = view.findViewById(R.id.editTextCorrectAnswer)
        editTextPl = view.findViewById(R.id.editTextPl)
        buttonAddWord = view.findViewById(R.id.buttonAddWord)
        buttonDeleteWord = view.findViewById(R.id.buttonDeleteWord)

        setupSpinner()
        setupSpinnerChooseCategoryToDelete()
        setupButton()

        return view
    }

    private fun setupSpinner() {
        // Get categories list from Firestore database
        val categories = mutableListOf<String>()
        val selectedCategory = categories.takeIf { it.isEmpty() } ?: "animals" // Jeśli selectedCategory jest puste, ustawia "animals"


        firestore.collection("words")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("name")
                    categoryName?.let { categories.add(it) }
                }

                // Add "Add new category" option
                categories.add("Add new category")
                categories.add("Delete word")

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
                    editTextCorrectAnswer.visibility = View.VISIBLE
                    editTextWrongAnswer1.visibility = View.VISIBLE
                    editTextWrongAnswer2.visibility = View.VISIBLE
                    editTextWrongAnswer3.visibility = View.VISIBLE
                    editTextPl.visibility = View.VISIBLE
                    buttonAddWord.visibility=View.VISIBLE
                    spinnerWord.visibility= View.GONE
                    buttonDeleteWord.visibility=View.GONE
                    spinnerCategoryDelete.visibility=View.GONE
                }
                 else if(selectedItem == "Delete word"){
                    editTextNewCategory.visibility = View.GONE
                    editTextCorrectAnswer.visibility = View.GONE
                    editTextWrongAnswer1.visibility = View.GONE
                    editTextWrongAnswer2.visibility = View.GONE
                    editTextWrongAnswer3.visibility = View.GONE
                    editTextPl.visibility = View.GONE
                    spinnerCategoryDelete.visibility= View.VISIBLE

                    spinnerWord.visibility= View.VISIBLE
                    buttonDeleteWord.visibility=View.VISIBLE
                    buttonAddWord.visibility=View.GONE
                    //if(!spinnerCategoryDelete.selectedItem.toString().isNullOrBlank()){fetchWordsForCategory(spinnerCategoryDelete.selectedItem.toString())}

                }
                else {
                    // Hide EditText field for new category
                    editTextNewCategory.visibility = View.GONE
                    editTextCorrectAnswer.visibility = View.VISIBLE
                    editTextWrongAnswer1.visibility = View.VISIBLE
                    editTextWrongAnswer2.visibility = View.VISIBLE
                    editTextWrongAnswer3.visibility = View.VISIBLE
                    editTextPl.visibility = View.VISIBLE
                    spinnerCategoryDelete.visibility= View.GONE
                    spinnerWord.visibility= View.GONE
                    buttonDeleteWord.visibility=View.GONE
                    buttonAddWord.visibility=View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing when no item is selected
            }
        }
    }
    private fun setupSpinnerChooseCategoryToDelete() {
        // Get categories list from Firestore database
        val categories = mutableListOf<String>()

        firestore.collection("words")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.getString("name")
                    categoryName?.let { categories.add(it) }
                }

                // Set adapter for Spinner
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategoryDelete.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        spinnerCategoryDelete.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                if (selectedCategory.isNotBlank()) {
                    fetchWordsForCategory(selectedCategory)
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

        buttonDeleteWord.setOnClickListener {
            val selectedCategory = spinnerCategoryDelete.selectedItem.toString()
             selectedWord = spinnerWord?.selectedItem.toString()
            Log.d("WordToDelete","${!selectedCategory.isNullOrBlank() && spinnerWord.selectedItem.toString().isNullOrBlank()}")

            // Sprawdź, czy wybrano kategorię i słowo
            if (!selectedCategory.isNullOrBlank() && !spinnerWord.selectedItem.toString().isNullOrBlank()) {
                // Usuń dokument dotyczący słowa dla każdego użytkownika
                firebaseOperations.deleteWordForAllUsers(selectedCategory, "word"+(spinnerWord.selectedItemPosition+1))
                deleteWordFromJson(selectedCategory, selectedWord)
            } else {
                // Komunikat o błędzie, jeśli kategoria lub słowo nie zostały wybrane
                Toast.makeText(requireContext(), "Please select a category and a word to delete", Toast.LENGTH_SHORT).show()
            }
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
    private fun fetchWordsForCategory(category: String) {
        val words = mutableListOf<String>()

        firestore.collection("words").document(category).collection("words")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val word = document.getString("pl")
                    word?.let { words.add(it) }
                }

                // Set adapter for SpinnerWord
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, words)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWord.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching words: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteWordFromJson(category: String, wordPl: String) {
        val storageFileRef = storageRef.child(jsonFilePath)
        storageFileRef.getBytes(1024 * 1024) // maximum size in bytes
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes)

                try {
                    // Parse existing JSON
                    val jsonObject = JSONObject(jsonString)

                    // Check if the category exists
                    if (jsonObject.has("categories")) {
                        val categoriesObject = jsonObject.getJSONObject("categories")

                        if (categoriesObject.has(category)) {
                            val categoryObject = categoriesObject.getJSONObject(category)
                            val wordsArray = categoryObject.getJSONArray("words")

                            // Find and remove the word from the words array
                            var wordFound = false
                            for (i in 0 until wordsArray.length()) {
                                val wordObject = wordsArray.getJSONObject(i)
                                if (wordObject.getString("pl") == wordPl) {
                                    wordsArray.remove(i)
                                    wordFound = true
                                    break
                                }
                            }

                            if (wordFound) {
                                // Replace the old words array with the new one
                                categoryObject.put("words", wordsArray)
                                categoriesObject.put(category, categoryObject)
                                jsonObject.put("categories", categoriesObject)

                                // Upload the updated JSON file back to Firebase Storage
                                uploadJsonToStorage(jsonObject.toString().toByteArray())
                                Toast.makeText(requireContext(), "Word deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Word not found in the selected category "+selectedWord, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Category not found in JSON", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid JSON format", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "JSON Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle error fetching JSON file from Firebase Storage
                Toast.makeText(requireContext(), "Error fetching JSON from storage: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun JSONArray.iterator(): MutableIterator<Any> {
        return object : MutableIterator<Any> {
            private var index = 0
            override fun hasNext(): Boolean = index < this@iterator.length()
            override fun next(): Any = this@iterator.get(index++)
            override fun remove() {
                val jsonArray = JSONArray()
                for (i in 0 until this@iterator.length()) {
                    if (i != index - 1) {
                        jsonArray.put(this@iterator.get(i))
                    }
                }
                (this@iterator as JSONArray).apply {
                    for (i in 0 until jsonArray.length()) {
                        this.put(i, jsonArray.get(i))
                    }
                    for (i in jsonArray.length() until this.length()) {
                        this.remove(jsonArray.length())
                    }
                }
                index--
            }
        }
    }

}
