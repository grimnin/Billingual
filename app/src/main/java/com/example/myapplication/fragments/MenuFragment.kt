package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.AdminPanel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMenuBinding
import com.example.myapplication.fragments.mistakes.MistakeFragment
import com.example.myapplication.fragments.quiz.QuizFragment
import com.example.myapplication.fragments.rank.Rank
import com.example.myapplication.fragments.settings.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MenuFragment : Fragment() {

    private lateinit var binding:FragmentMenuBinding
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMenuBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        hideAdminPanel()

        binding.QuizImage.setOnClickListener {
            val fragment= QuizFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
        binding.MistakesImage.setOnClickListener {
            val fragment= MistakeFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
        binding.RankingsImage.setOnClickListener {
            val fragment= Rank()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
        binding.SettingsImage.setOnClickListener {
            val fragment= SettingsFragment()
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment)
                .commit()
        }
        showAdminPanel()
// w metodzie onViewCreated klasy MenuFragment
binding.AdminImage.setOnClickListener {
    val intent = Intent(requireContext(), AdminPanel::class.java)
    startActivity(intent)
}

    }
private fun showAdminPanel(){
    val currentUser = FirebaseAuth.getInstance().currentUser
    if(currentUser != null) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val role = document.getString("role")
                    if (role == "creator"||role=="mod") {
                        // Pokaż widoki związane z administratorem
                        binding.AdminImage.visibility = View.VISIBLE
                        binding.AdminTextView.visibility = View.VISIBLE
                    } else {
                        // Ukryj widoki związane z administratorem
                        binding.AdminImage.visibility = View.GONE
                        binding.AdminTextView.visibility = View.GONE
                    }
                } else {
                    // Dokument użytkownika nie istnieje
                }
            }
            .addOnFailureListener { exception ->
                // Obsługa błędów odczytu dokumentu
            }
    } else {
        // Użytkownik niezalogowany
    }
}
    private fun hideAdminPanel() {
        binding.AdminImage.visibility = View.GONE
        binding.AdminTextView.visibility = View.GONE
    }

}