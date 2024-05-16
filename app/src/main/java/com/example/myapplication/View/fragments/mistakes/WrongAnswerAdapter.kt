
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.data.WrongAnswerWords
import com.example.myapplication.Model.repository.FirebaseOperations
import com.example.myapplication.R

class WrongAnswerWordsAdapter(
    private val context: Context,
    private val wordsList: MutableList<WrongAnswerWords>
) : RecyclerView.Adapter<WrongAnswerWordsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plTextView: TextView = itemView.findViewById(R.id.tvPolishTranslation)
        val engTextView: TextView = itemView.findViewById(R.id.tvEnglishTranslation)
        val correctCountTextView: TextView = itemView.findViewById(R.id.tvCorrectCount)
        val wrongCountTextView: TextView = itemView.findViewById(R.id.tvWrongCount)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        init {
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val word = wordsList[position]
                    Log.d("Word", "word${position} mistake status updated successfully.")
                    val firebaseOperations = FirebaseOperations(context)
                    firebaseOperations.updateMadeMistakeValue(word.id,word.eng, false)

                    wordsList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wrong_answer_word, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentWord = wordsList[position]
        holder.plTextView.text = "Polish: ${currentWord.pl}"
        holder.engTextView.text = "English: ${currentWord.eng}"
        holder.correctCountTextView.text = "Correct Count: ${currentWord.correctCount}"
        holder.wrongCountTextView.text = "Wrong Count: ${currentWord.mistakeCounter}"
    }

    override fun getItemCount(): Int {
        return wordsList.size
    }



}