package com.example.myapplication.View.fragments.mistakes
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.data.IrregularVerb
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R

class IrregularVerbsAdapter(
    private val context: Context,
    private val irregularVerbsList: MutableList<IrregularVerb> // Zmieniamy na MutableList
) : RecyclerView.Adapter<IrregularVerbsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val baseTextView: TextView = itemView.findViewById(R.id.baseTextView)
        val pastSimpleTextView: TextView = itemView.findViewById(R.id.pastSimpleTextView)
        val pastParticipleTextView: TextView = itemView.findViewById(R.id.pastParticipleTextView)
        val meaningTextView: TextView = itemView.findViewById(R.id.meaningTextView)
        val correctAnswersTextView: TextView = itemView.findViewById(R.id.correctAnswersTextView)
        val wronAnswersTextView: TextView = itemView.findViewById(R.id.wrongAnswersTextView)
        val madeMistakeTextView: TextView = itemView.findViewById(R.id.madeMistakeTextView)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteFromVerbs)

        init {
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val verb = irregularVerbsList[position]
                    // Tutaj wykonujemy operację na FirebaseOperations
                    val firebaseOperations = FirebaseOperations(context)
                    firebaseOperations.updateVerbMistakeStatus(verb.id, false)

                    // Usuwamy pozycję z listy
                    irregularVerbsList.removeAt(position)

                    // Aktualizujemy UI, aby usunięta pozycja zniknęła z widoku
                    notifyItemRemoved(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_irregular_verb_mistake, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentVerb = irregularVerbsList[position]
        holder.baseTextView.text = "Base: ${currentVerb.base}"
        holder.pastSimpleTextView.text = "Past Simple: ${currentVerb.pastSimple}"
        holder.pastParticipleTextView.text = "Past Participle: ${currentVerb.pastParticiple}"
        holder.meaningTextView.text = "Meaning: ${currentVerb.meaning}"
        holder.correctAnswersTextView.text = "Correct Answers: ${currentVerb.correctAnswers}"
        holder.wronAnswersTextView.text = "Wrong Answers: ${currentVerb.wrongAnswers}"
        holder.madeMistakeTextView.text = "Made Mistake: ${currentVerb.madeMistake}"
    }

    override fun getItemCount(): Int {
        return irregularVerbsList.size
    }
}
