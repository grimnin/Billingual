
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.data.WrongAnswerWords
import com.example.myapplication.R

class WrongAnswersAdapter(
    private val wrongAnswersListWords: List<WrongAnswerWords>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<WrongAnswersAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plTextView: TextView = itemView.findViewById(R.id.tvPolishTranslation)
        val engTextView: TextView = itemView.findViewById(R.id.tvEnglishTranslation)
        val correctCountTextView: TextView = itemView.findViewById(R.id.tvCorrectCount)
        val wrongCountTextView: TextView = itemView.findViewById(R.id.tvWrongCount)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_word_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wrongAnswer = wrongAnswersListWords[position]
        holder.plTextView.text = wrongAnswer.pl
        holder.engTextView.text = wrongAnswer.eng
        holder.correctCountTextView.text = wrongAnswer.correctCount.toString()
        holder.wrongCountTextView.text = wrongAnswer.mistakeCounter.toString()
    }

    override fun getItemCount(): Int {
        return wrongAnswersListWords.size
    }
}

