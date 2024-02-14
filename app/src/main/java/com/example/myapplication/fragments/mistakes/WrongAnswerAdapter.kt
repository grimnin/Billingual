import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.fragments.mistakes.WrongAnswer

class WrongAnswersAdapter(
    private val wrongAnswersList: List<WrongAnswer>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<WrongAnswersAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plTextView: TextView = itemView.findViewById(R.id.plTextView)
        val engTextView: TextView = itemView.findViewById(R.id.engTextView)

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
        val wrongAnswer = wrongAnswersList[position]
        holder.plTextView.text = wrongAnswer.pl
        holder.engTextView.text = wrongAnswer.eng
    }

    override fun getItemCount(): Int {
        return wrongAnswersList.size
    }
}
