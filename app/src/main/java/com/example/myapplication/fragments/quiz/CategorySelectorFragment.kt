package com.example.myapplication.fragments.quiz

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class CategorySelectorFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_category_selector, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()

        // Wyświetlanie okna dialogowego z kategoriami
        fetchCategoriesAndShowDialog(rootView)

        return rootView
    }

    private fun fetchCategoriesAndShowDialog(rootView: View) {
        val categories = ArrayList<String>()

        // Pobierz kategorie z Firestore
        firestore.collection("words")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val categoryName = document.getString("name")
                    categoryName?.let {
                        categories.add(it)
                    }
                }
                showCategoryDialog(rootView, categories)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCategoryDialog(rootView: View, categories: List<String>) {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        val checkBoxes = ArrayList<CheckBox>()

        for (category in categories) {
            val checkBox = CheckBox(requireContext())
            checkBox.text = category
            layout.addView(checkBox)
            checkBoxes.add(checkBox)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Categories")
        builder.setView(layout)
        builder.setPositiveButton("Start Quiz") { dialog, _ ->
            val selectedCategories = ArrayList<String>()
            for (checkBox in checkBoxes) {
                if (checkBox.isChecked) {
                    selectedCategories.add(checkBox.text.toString())
                }
            }
            saveSelectedCategories(selectedCategories)
            Toast.makeText(requireContext(), "Quiz will start with selected categories.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun saveSelectedCategories(selectedCategories: List<String>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet("selected_categories", HashSet(selectedCategories))
        editor.apply()
    }
}
