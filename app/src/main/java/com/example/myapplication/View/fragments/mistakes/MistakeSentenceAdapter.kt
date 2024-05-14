package com.example.myapplication.View.fragments.mistakes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R
import com.example.myapplication.View.fragments.grammar.Sentence

class MistakeSentenceAdapter(private val firebaseOperations: FirebaseOperations) : RecyclerView.Adapter<MistakeSentenceAdapter.ViewHolder>() {

    private var sentences: List<Sentence> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSentence: TextView = itemView.findViewById(R.id.textViewSentence)
        private val textViewZdanie: TextView = itemView.findViewById(R.id.textViewZdanie)
        private val textViewTense: TextView = itemView.findViewById(R.id.textViewTense)
        private val textViewCorrect: TextView = itemView.findViewById(R.id.textViewCorrect)
        private val textViewWrong: TextView = itemView.findViewById(R.id.textViewWrong)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDeleteSentenceFromMistakes)

        fun bind(sentence: Sentence) {
            textViewSentence.text = "Sentence: ${sentence.sentence}"
            textViewZdanie.text = "Zdanie: ${sentence.zdanie}"
            textViewTense.text = "Tense: ${sentence.tense}"
            textViewCorrect.text = "Correct Answers: ${sentence.correctAnswers}"
            textViewWrong.text = "Wrong Answers: ${sentence.wrongAnswers}"

            buttonDelete.setOnClickListener {
                // Wywołanie funkcji do aktualizacji statusu błędu
                firebaseOperations.updateMistakeSentenceStatus(sentence.id)
                // Usunięcie zdania z listy
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    sentences = sentences.filterIndexed { index, _ -> index != position }
                    notifyItemRemoved(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mistake_sentence, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sentences[position])
    }

    override fun getItemCount(): Int {
        return sentences.size
    }

    fun setData(sentences: List<Sentence>) {
        this.sentences = sentences
        notifyDataSetChanged()
    }
}
