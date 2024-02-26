package com.example.myapplication.fragments.rank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.User
import com.example.myapplication.databinding.FragmentRankBinding
import com.example.myapplication.fragments.MenuFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Rank : Fragment() {
    private lateinit var binding: FragmentRankBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var rankAdapter: RankAdapter // Adapter dla RecyclerView
    private val userList: MutableList<User> = mutableListOf() // Lista użytkowników
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRankBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewRank
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get users and populate the list
        getUsersAndPopulateList()

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        rankAdapter = RankAdapter(userList)
        recyclerView.adapter = rankAdapter

        // Setup search functionality
        setupSearchFunctionality()

        binding.buttonReturn.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager

            val rankFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)
            rankFragment?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }

            val menuFragment = MenuFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, menuFragment)
                .commit()
        }
    }

    // Function to setup search functionality
    private fun setupSearchFunctionality() {
        binding.buttonSearch.setOnClickListener {
            val searchQuery = binding.editTextSearch.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                val position = userList.indexOfFirst { it.login == searchQuery }
                if (position != -1) {
                    // Przewiń RecyclerView do znalezionej pozycji
                    recyclerView.scrollToPosition(position)

                    // Przewiń RecyclerView w taki sposób, aby użytkownik był widoczny na górze
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    layoutManager.scrollToPositionWithOffset(position, 0)
                }
            }
        }
    }


    private fun addDummyData() {
        // Add dummy data for testing scroll position
        repeat(30) {
            val dummyUser = User("DummyUserID$it", "dummy$it@example.com", "DummyUser$it", "user",15)
            userList.add(dummyUser)
        }
    }

    private fun getUsersAndPopulateList() {
        var position:Int=0
        // Fetch users from Firestore and populate the list
        firestore.collection("users")
            .orderBy("score", Query.Direction.DESCENDING) // Sort users by score in descending order
            .get()
            .addOnSuccessListener { documents ->
                var currentUserPosition:Int = -1
                val currentUserUid = auth.currentUser?.uid

                userList.clear() // Wyczyszczenie listy przed dodaniem nowych użytkowników
                addDummyData()

                for ((index, document) in documents.withIndex()) {
                    // Convert Firestore document to User object
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }

                for (user in userList){
                    position++
                    if (user.uid.equals(currentUserUid) ) {
                        currentUserPosition = position
                        break
                    }
                }

                // Notify the adapter that the data set has changed
                rankAdapter.notifyDataSetChanged()
                // Scroll RecyclerView to current user position
                recyclerView.scrollToPosition(currentUserPosition)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }
    }
}
