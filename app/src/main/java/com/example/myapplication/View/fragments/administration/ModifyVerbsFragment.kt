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
import androidx.fragment.app.Fragment
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONException
import org.json.JSONObject

class ModifyVerbsFragment : Fragment() {

    private lateinit var spinnerDeleteVerbs: Spinner
    private lateinit var editTextTextPL: EditText
    private lateinit var editTextTextBasic: EditText
    private lateinit var editTextTextSimple: EditText
    private lateinit var editTextTextPerfect: EditText
    private lateinit var buttonAddVerb: Button
    private lateinit var buttonDeleteVerb: Button
    private lateinit var firebaseOperations: FirebaseOperations

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseOperations = FirebaseOperations(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_verbs, container, false)

        spinnerDeleteVerbs = view.findViewById(R.id.spinnerDeleteVerbs)
        editTextTextPL = view.findViewById(R.id.editTextTextPL)
        editTextTextBasic = view.findViewById(R.id.editTextTextBasic)
        editTextTextSimple = view.findViewById(R.id.editTextTextSimple)
        editTextTextPerfect = view.findViewById(R.id.editTextTextPerfect)
        buttonAddVerb = view.findViewById(R.id.buttonAddVerb)
        buttonDeleteVerb = view.findViewById(R.id.buttonDeleteVerb)

        loadIrregularVerbsFromJson()

        spinnerDeleteVerbs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Add Verb") {
                    // Pokaż pola tekstowe i przycisk delete
                    showFieldsAndAddButton()
                } else {
                    // Ukryj pola tekstowe i przycisk delete
                    hideFieldsAndAddButton()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie robimy nic w przypadku braku wybranego elementu
            }
        }

        buttonAddVerb.setOnClickListener {
            if (areFieldsFilled()) {
                addNewVerbToFirestoreForAllUsers()
                updateAndUploadJsonFileFromEditTextFields()
            } else {
                // Poinformuj użytkownika, że musi wypełnić wszystkie pola
            }
        }

        buttonDeleteVerb.setOnClickListener {
            val selectedItem = spinnerDeleteVerbs.selectedItem.toString()
            if (selectedItem != "Add Verb") {
                firebaseOperations.deleteVerbForAllUsers(selectedItem)
                deleteVerb(selectedItem)
            } else {
                // Poinformuj użytkownika, że nie można usunąć czasownika "Add Verb"
            }
        }

        return view
    }

    private fun deleteVerb(verbToDelete: String) {
        try {
            val storageRef = Firebase.storage.reference
            val jsonRef = storageRef.child("verbs.json")

            jsonRef.getBytes(MAX_DOWNLOAD_SIZE).addOnSuccessListener { bytes ->
                val jsonString = String(bytes)
                val jsonObject = JSONObject(jsonString)

                val verbsArray = jsonObject.getJSONArray("verbs")

                // Szukanie i usuwanie czasownika
                for (i in 0 until verbsArray.length()) {
                    val verbObject = verbsArray.getJSONObject(i)
                    val pl = verbObject.getString("PL")
                    if (pl == verbToDelete) {
                        verbsArray.remove(i)
                        break
                    }
                }

                // Aktualizacja obiektu JSON z nową tablicą czasowników
                jsonObject.put("verbs", verbsArray)

                // Wysyłanie zaktualizowanego JSON-a do Firebase Storage
                val updatedJsonString = jsonObject.toString()
                jsonRef.putBytes(updatedJsonString.toByteArray()).addOnSuccessListener {
                    Log.d(TAG, "Updated JSON file successfully.")
                    // Ponowne wczytanie danych z JSON
                    loadIrregularVerbsFromJson()
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating JSON file", exception)
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error updating JSON file", e)
        }
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
        val irregularVerbs = mutableListOf<String>()

        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("verbs")

            for (i in 0 until jsonArray.length()) {
                val verbObject = jsonArray.getJSONObject(i)
                val meaning = verbObject.getString("PL")

                irregularVerbs.add(meaning)
            }
            irregularVerbs.add("Add Verb")

            // Ustawienie danych w spinnerze
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, irregularVerbs)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDeleteVerbs.adapter = adapter

        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON", e)
        }
    }

    private fun showFieldsAndAddButton() {
        editTextTextPL.visibility = View.VISIBLE
        editTextTextBasic.visibility = View.VISIBLE
        editTextTextSimple.visibility = View.VISIBLE
        editTextTextPerfect.visibility = View.VISIBLE
        buttonAddVerb.visibility = View.VISIBLE
        buttonDeleteVerb.visibility = View.GONE
    }

    private fun hideFieldsAndAddButton() {
        editTextTextPL.visibility = View.GONE
        editTextTextBasic.visibility = View.GONE
        editTextTextSimple.visibility = View.GONE
        editTextTextPerfect.visibility = View.GONE
        buttonDeleteVerb.visibility = View.VISIBLE
        buttonAddVerb.visibility = View.GONE
    }

    private fun areFieldsFilled(): Boolean {
        return editTextTextPL.text.isNotEmpty() &&
                editTextTextBasic.text.isNotEmpty() &&
                editTextTextSimple.text.isNotEmpty() &&
                editTextTextPerfect.text.isNotEmpty()
    }

    private fun addNewVerbToFirestoreForAllUsers() {
        val db = Firebase.firestore

        // Pobierz wartość ID z pola spinnerDeleteVerbs
        val selectedItemId = spinnerDeleteVerbs.selectedItemPosition + 1
        val newVerbData = hashMapOf(
            "id" to "v$selectedItemId",
            "pl" to editTextTextPL.text.toString(),
            "Basic" to editTextTextBasic.text.toString(),
            "PastSimple" to editTextTextSimple.text.toString(),
            "PastPerfect" to editTextTextPerfect.text.toString(),
            "stats" to hashMapOf(
                "correctAnswers" to 0,
                "wrongAnswers" to 0,
                "madeMistake" to false
            )
        )

        val usersCollectionRef = db.collection("users")

        usersCollectionRef.get()
            .addOnSuccessListener { users ->
                for (user in users) {
                    val userId = user.id
                    val userVerbCollectionRef = usersCollectionRef.document(userId)
                        .collection("stats").document("grammar_stats")
                        .collection("grammar").document("irregular_verbs")
                        .collection("verbs")

                    // Ustaw nowy dokument z ręcznie ustawionym ID
                    userVerbCollectionRef.document("v$selectedItemId").set(newVerbData)
                        .addOnSuccessListener {
                            Log.d(TAG, "New verb added with ID: verb$selectedItemId for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding new verb for user: $userId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching users", e)
            }
        val verbsCollectionRef = db.collection("grammar").document("irregular_verbs").collection("verbs")

        // Dodaj nowy dokument do kolekcji irregular_verbs/verbs
        verbsCollectionRef.document("v$selectedItemId").set(newVerbData)
            .addOnSuccessListener { documentReference ->

                // Po dodaniu czasownika, ponownie wczytaj dane JSON
                loadIrregularVerbsFromJson()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding new verb", e)
            }

    }

    private fun updateAndUploadJsonFileFromEditTextFields() {
        val base = editTextTextBasic.text.toString()
        val pastSimple = editTextTextSimple.text.toString()
        val pastParticiple = editTextTextPerfect.text.toString()
        val pl = editTextTextPL.text.toString()

        try {
            val storageRef = Firebase.storage.reference
            val jsonRef = storageRef.child("verbs.json")

            jsonRef.getBytes(MAX_DOWNLOAD_SIZE).addOnSuccessListener { bytes ->
                val jsonString = String(bytes)
                val jsonObject = JSONObject(jsonString)

                val verbsArray = jsonObject.getJSONArray("verbs")

                // Tworzenie nowego czasownika
                val newVerb = JSONObject()
                newVerb.put("Base", base)
                newVerb.put("Past-Simple", pastSimple)
                newVerb.put("Past-Participle", pastParticiple)
                newVerb.put("PL", pl)

                // Dodawanie nowego czasownika do tablicy czasowników
                verbsArray.put(newVerb)

                // Aktualizacja obiektu JSON z nową tablicą czasowników
                jsonObject.put("verbs", verbsArray)

                // Wysyłanie zaktualizowanego JSON-a do Firebase Storage
                val updatedJsonString = jsonObject.toString()
                jsonRef.putBytes(updatedJsonString.toByteArray()).addOnSuccessListener {
                    Log.d(TAG, "Updated JSON file successfully.")
                    editTextTextBasic.setText("")
                    editTextTextSimple.setText("")
                    editTextTextPerfect.setText("")
                    editTextTextPL.setText("")
                    loadIrregularVerbsFromJson()
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating JSON file", exception)
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error updating JSON file", e)
        }
    }








    companion object {
        private const val TAG = "ModifyVerbsFragment"
        private const val MAX_DOWNLOAD_SIZE: Long = 1024 * 1024 // 1 MB max download size
    }
}
