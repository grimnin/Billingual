import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.fragments.mistakes.WrongAnswerWords

class WrongAnswersAdapter(
    private val wrongAnswersListWords: List<WrongAnswerWords>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<WrongAnswersAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plTextView: TextView = itemView.findViewById(R.id.plTextView)

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
            .inflate(R.layout.item_wrong_answer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wrongAnswer = wrongAnswersListWords[position]
        holder.plTextView.text = wrongAnswer.pl
    }

    override fun getItemCount(): Int {
        return wrongAnswersListWords.size
    }
}

