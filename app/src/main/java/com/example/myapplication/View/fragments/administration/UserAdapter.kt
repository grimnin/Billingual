package com.example.myapplication.View.fragments.administration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.data.User
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(private val userList: List<User>, private val onDataSetChanged: () -> Unit) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nickTextView: TextView = itemView.findViewById(R.id.nickTextView)
        private val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        private val pointsTextView: TextView = itemView.findViewById(R.id.pointsTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val changeRoleButton: Button = itemView.findViewById(R.id.changeRoleButton)

        fun bind(user: User) {
            // Ustaw dane użytkownika w widoku ViewHolder
            nickTextView.text = user.login
            roleTextView.text = user.role
            pointsTextView.text = user.score.toString()

            // Możesz również ustawić słuchaczy dla przycisków, jeśli jest to konieczne
            if (!user.role.equals("creator")) {
                deleteButton.setOnClickListener { deleteUser(user) }
                changeRoleButton.setOnClickListener { changeUserRole(user) }
            }
        }
    }

    private fun deleteUser(user: User) {
        val db = FirebaseFirestore.getInstance()

        // Usuń dokument użytkownika z kolekcji Firestore
        db.collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // Jeśli usunięcie zakończy się sukcesem, wykonaj odświeżenie
                onDataSetChanged.invoke()
            }
            .addOnFailureListener { e ->
                // Obsłuż ewentualny błąd
            }
    }

    private fun changeUserRole(user: User) {
        val db = FirebaseFirestore.getInstance()

        // Zmiana roli użytkownika
        val newRole = if (user.role.equals("user") ) "mod" else "user"
        db.collection("users").document(user.uid)
            .update("role", newRole)
            .addOnSuccessListener {
                // Jeśli zmiana zakończy się sukcesem, wykonaj odświeżenie
                onDataSetChanged.invoke()
            }
            .addOnFailureListener { e ->
                // Obsłuż ewentualny błąd
            }
    }
}

