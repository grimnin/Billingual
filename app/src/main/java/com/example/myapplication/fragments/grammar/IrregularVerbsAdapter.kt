package com.example.myapplication.fragments.grammar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class IrregularVerbsAdapter(private val irregularVerbs: List<IrregularVerb>) :
    RecyclerView.Adapter<IrregularVerbsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_irregular_verb, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val irregularVerb = irregularVerbs[position]
        holder.meaningTextView.text = irregularVerb.meaning
        holder.baseTextView.text = irregularVerb.base
        holder.pastSimpleTextView.text = irregularVerb.pastSimple
        holder.pastParticipleTextView.text = irregularVerb.pastParticiple

    }

    override fun getItemCount(): Int {
        return irregularVerbs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val meaningTextView: TextView = itemView.findViewById(R.id.textViewVerbPL)
        val baseTextView: TextView = itemView.findViewById(R.id.textViewBase)
        val pastSimpleTextView: TextView = itemView.findViewById(R.id.textViewPastSimple)
        val pastParticipleTextView: TextView = itemView.findViewById(R.id.textViewPastParticiple)

    }
}

