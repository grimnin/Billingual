package com.example.myapplication.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class AddWordViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    fun fetchCategories() {
        firestore.collection("words").get()
            .addOnSuccessListener { result ->
                val categories = result.mapNotNull { it.getString("name") }
                _categories.postValue(categories)
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    // Other Firebase operations can also be added here
}
