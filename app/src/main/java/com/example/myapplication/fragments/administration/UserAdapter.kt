package com.example.myapplication.fragments.administration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.User

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

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
            deleteButton.setOnClickListener { /* Obsługa kliknięcia */ }
            changeRoleButton.setOnClickListener { /* Obsługa kliknięcia */ }
        }
    }

}
