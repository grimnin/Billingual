package com.example.myapplication.View.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.View.panels.MistakeContainer
import com.example.myapplication.View.fragments.quiz.QuizFragment
import com.example.myapplication.View.fragments.rank.Rank
import com.example.myapplication.View.fragments.settings.SettingsFragment
import com.example.myapplication.View.panels.AdminPanel
import com.example.myapplication.View.panels.GrammarPanelActivity
import com.example.myapplication.databinding.FragmentMenuBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.QuizImage.setOnClickListener {
            showCategorySelectionDialog()
        }
        binding.GramaticImage.setOnClickListener {
            redirectToGrammarPanel()
        }

        binding.MistakesImage.setOnClickListener {
            redirectToMistakeFragment()
        }

        binding.RankingsImage.setOnClickListener {
            redirectToRankFragment()
        }

        binding.SettingsImage.setOnClickListener {
            redirectToSettingsFragment()
        }

        binding.AdminImage.setOnClickListener {
            redirectToAdminPanel()
        }

        hideAdminPanel()
    }

    private fun redirectToGrammarPanel() {
        val intent = Intent(requireContext(), GrammarPanelActivity::class.java)
        startActivity(intent)
    }

    private fun showCategorySelectionDialog() {
        val categories = mutableListOf<String>()

        // Dodaj "All" jako pierwszą pozycję
        categories.add("All")

        // Pobierz kategorie z firestore
        firestore.collection("words")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val categoryName = document.getString("name")
                    categoryName?.let { categories.add(it) }
                }

                // Utwórz tablicę reprezentującą zaznaczenie dla każdej kategorii
                val checkedItems = BooleanArray(categories.size) { false }

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select Categories")
                builder.setMultiChoiceItems(categories.toTypedArray(), checkedItems) { _, which, isChecked ->
                    // Obsługa zdarzenia kliknięcia na pozycję
                    if (which == 0) {
                        // Jeśli kliknięto "All", zaznacz lub odznacz wszystkie pozycje
                        for (i in checkedItems.indices) {
                            checkedItems[i] = isChecked
                            dialog.listView.setItemChecked(i, isChecked)
                        }
                    } else {
                        // Jeśli kliknięto inną kategorię, zaktualizuj jej stan
                        checkedItems[which] = isChecked
                        // Jeśli odznaczono którąś z kategorii, odznacz "All"
                        if (!isChecked && checkedItems[0]) {
                            checkedItems[0] = false
                            dialog.listView.setItemChecked(0, false)
                        }
                        // Jeśli odznaczono "All", odznacz wszystkie kategorie
                        if (which == 0 && !isChecked) {
                            for (i in 1 until checkedItems.size) {
                                checkedItems[i] = false
                                dialog.listView.setItemChecked(i, false)
                            }
                        }
                    }
                }
                builder.setPositiveButton("Start Quiz") { _, _ ->
                    // Przygotuj listę wybranych kategorii
                    val selectedCategories = mutableListOf<String>()
                    for (i in 1 until categories.size) {
                        if (checkedItems[i]) {
                            selectedCategories.add(categories[i])
                        }
                    }

                    // Sprawdź łączną liczbę słów w wybranych kategoriach
                    var totalWordsCount = 0
                    val tasks = mutableListOf<Task<QuerySnapshot>>()
                    for (category in selectedCategories) {
                        val task = firestore.collection("words").document(category)
                            .collection("words")
                            .get()
                            .addOnSuccessListener { documents ->
                                totalWordsCount += documents.size()
                            }
                            .addOnFailureListener { exception ->
                                // Obsłuż błąd pobierania dokumentów
                                Toast.makeText(requireContext(), "Failed to fetch documents for category: $category, ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        tasks.add(task)
                    }

                    Tasks.whenAllComplete(tasks)
                        .addOnSuccessListener {
                            if (totalWordsCount > 4) {
                                // Jeśli suma jest większa niż 4, przekieruj do fragmentu quizu
                                saveSelectedCategories(selectedCategories)
                                redirectToQuizFragment()
                            } else {
                                // Jeśli suma jest mniejsza lub równa 4, wyświetl komunikat
                                Toast.makeText(requireContext(), "Selected categories contain less than 4 words for the quiz.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                dialog = builder.create()
                dialog.show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSelectedCategories(selectedCategories: List<String>) {
        // Odfiltruj kategorię "All"
        val filteredCategories = selectedCategories.filter { it != "All" }
        val editor = sharedPreferences.edit()
        editor.putStringSet("selectedCategories", filteredCategories.toSet())
        editor.apply()
    }

    private fun redirectToMistakeFragment() {
        val intent = Intent(requireContext(), MistakeContainer::class.java)
        startActivity(intent)
    }

    private fun redirectToRankFragment() {
        val fragment = Rank()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, fragment)
            .commit()
    }

    private fun redirectToSettingsFragment() {
        val fragment = SettingsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, fragment)
            .commit()
    }

    private fun redirectToAdminPanel() {
        val intent = Intent(requireContext(), AdminPanel::class.java)
        startActivity(intent)
    }

    private fun hideAdminPanel() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        if (role == "creator" || role == "mod") {
                            // Show admin-related views
                            binding.AdminImage.visibility = View.VISIBLE
                            binding.AdminTextView.visibility = View.VISIBLE
                        } else {
                            // Hide admin-related views
                            binding.AdminImage.visibility = View.GONE
                            binding.AdminTextView.visibility = View.GONE
                        }
                    } else {
                        // User document does not exist
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle document read failure
                }
        } else {
            // User not logged in
            binding.AdminImage.visibility = View.GONE
            binding.AdminTextView.visibility = View.GONE
        }
    }

    private fun redirectToQuizFragment() {
        val fragment = QuizFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView2, fragment)
            .commit()
    }
}
