package com.example.myapplication.fragments.rank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.User
import com.google.firebase.auth.FirebaseAuth

class RankAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<RankAdapter.RankViewHolder>() {

    inner class RankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loginTextView: TextView = itemView.findViewById(R.id.loginTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_rank, parent, false)
        return RankViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.loginTextView.text = currentUser.login
        holder.scoreTextView.text = currentUser.score.toString().plus(" pt")

        // Change background color for the current user
        if (currentUser.uid == FirebaseAuth.getInstance().currentUser?.uid) {
            holder.itemView.setBackgroundResource(R.color.user_position) // Assuming you have a color resource named "teal_700"
        } else {
            // Reset background color for other users
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
