package com.example.myapplication.View.fragments.administration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Model.data.User
import com.example.myapplication.databinding.FragmentUserManagementBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserManagementFragment : Fragment() {

    private lateinit var binding: FragmentUserManagementBinding
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchUserData()
    }

    private fun setupRecyclerView() {
        binding.userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(userList) {
            fetchUserData() // Odśwież dane po wykonaniu akcji przez adapter
        }
        binding.userRecyclerView.adapter = userAdapter
    }

    private fun fetchUserData() {
        // Czyścimy listę przed pobraniem nowych danych
        userList.clear()

        // Pobierz kolekcję "users" z Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                // Dodaj dane użytkowników do listy
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                // Zaktualizuj adapter RecyclerView po pobraniu danych
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Obsłuż błąd pobierania danych
            }
    }
}
