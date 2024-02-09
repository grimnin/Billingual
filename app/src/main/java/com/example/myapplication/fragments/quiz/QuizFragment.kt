import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentQuizBinding
import com.example.myapplication.fragments.quiz.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject

class QuizFragment : Fragment() {

    private lateinit var binding: FragmentQuizBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isCorrect = false
    private var attemptCount = 0
    private val storage = Firebase.storage
    private var isAnswered = false
    private val selectedWords = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get and display a random word for the quiz
        getRandomWordForQuiz()

        // Set onClickListener for answer buttons
        binding.apply {
            buttonAnswer1.setOnClickListener { checkAnswer(buttonAnswer1.text.toString()) }
            buttonAnswer2.setOnClickListener { checkAnswer(buttonAnswer2.text.toString()) }
            buttonAnswer3.setOnClickListener { checkAnswer(buttonAnswer3.text.toString()) }
            buttonAnswer4.setOnClickListener { checkAnswer(buttonAnswer4.text.toString()) }
        }
    }

    private fun getRandomWordForQuiz() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            if (attemptCount < 5) {
                isCorrect = false

                firestore.collection("users").document(userId).collection("stats")
                    .document("word_stats").collection("categories").get()
                    .addOnSuccessListener { categories ->
                        val allWords = mutableListOf<Word>()
                        val categoryCount = categories.size()
                        var categoriesProcessed = 0
                        for (category in categories) {
                            val categoryId = category.id
                            firestore.collection("users").document(userId).collection("stats")
                                .document("word_stats").collection("categories").document(categoryId)
                                .collection("words").get()
                                .addOnSuccessListener { words ->
                                    for (wordDoc in words) {
                                        val word = wordDoc.toObject(Word::class.java)
                                        allWords.add(word)
                                    }
                                    categoriesProcessed++
                                    if (categoriesProcessed == categoryCount) {
                                        var randomWord: Word? = null
                                        do {
                                            randomWord = selectRandomWord(allWords)
                                        } while (randomWord != null && selectedWords.contains(randomWord.pl))

                                        if (randomWord != null) {
                                            selectedWords.add(randomWord.pl)
                                            val wordEng = randomWord.pl
                                            binding.QuestiontextView.text = wordEng
                                            loadAnswersFromStorage(randomWord.pl)
                                            attemptCount++
                                        } else {
                                            Log.d("QuizFragment", "No available words")
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    categoriesProcessed++
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("QuizFragment", "Error getting categories", exception)
                    }
            } else {
                Log.d("QuizFragment", "User has used all attempts")
            }
        }
    }

    private fun selectRandomWord(wordsList: List<Word>): Word? {
        val selectableWords = mutableListOf<Word>()
        for (word in wordsList) {
            selectableWords.add(word)
        }
        if (selectableWords.isEmpty()) {
            return null
        }
        val weights = mutableListOf<Int>()
        for (word in selectableWords) {
            val errorCount = word.mistakeCounter + 1
            val correctCount = word.correctCount + 1
            val ratio = if (correctCount > 0) errorCount.toDouble() / correctCount.toDouble() else errorCount.toDouble() + 1
            val weight = (ratio * 10).toInt() + 1
            repeat(weight) {
                weights.add(selectableWords.indexOf(word))
            }
        }
        val randomIndex = (weights.indices).random()
        val selectedWord = selectableWords[weights[randomIndex]]
        Log.d("QuizFragment", "Random word: ${selectedWord.eng}, mistakeCounter: ${selectedWord.mistakeCounter}, correctCount: ${selectedWord.correctCount}, total: ${selectedWord.total}")
        return selectedWord
    }

    // Gdy użytkownik wybierze odpowiedź
    private fun checkAnswer(selectedAnswer: String) {
        if (!isAnswered) {
            isAnswered = true

            disableAnswerButtons()
            isCorrect = checkIfAnswerIsCorrect(selectedAnswer)
            Log.d("Boo", "$isCorrect")

            // Zmiana tła przycisku na podstawie poprawności odpowiedzi


            if (attemptCount < 5) {
                // Resetowanie tła przycisków po kilku sekundach
                Handler(Looper.getMainLooper()).postDelayed({
                    resetButtonBackgrounds()
                    getRandomWordForQuiz()
                }, 1000) // 2000 ms = 2 sekundy
            }
        }
    }

    // Resetowanie tła przycisków do domyślnego
    private fun resetButtonBackgrounds() {
        binding.apply {
            buttonAnswer1.setBackgroundResource(R.drawable.rectangle)
            buttonAnswer2.setBackgroundResource(R.drawable.rectangle)
            buttonAnswer3.setBackgroundResource(R.drawable.rectangle)
            buttonAnswer4.setBackgroundResource(R.drawable.rectangle)
        }
    }


    private fun updateAnswerIndicator(isCorrect: Boolean) {
        val indicatorList = listOf(
            binding.answerIndicator1,
            binding.answerIndicator2,
            binding.answerIndicator3,
            binding.answerIndicator4,
            binding.answerIndicator5
        )
        val index = attemptCount-1
        if (index < indicatorList.size) {
            if (isCorrect) {
                indicatorList[index].setImageResource(R.drawable.ic_circle_green)
            } else {
                indicatorList[index].setImageResource(R.drawable.ic_circle_red)
            }
        }
    }

    private fun updateWordStats(isCorrect: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val currentWord = binding.QuestiontextView.text.toString()
            firestore.collection("users").document(userId).collection("stats")
                .document("word_stats").collection("categories").get()
                .addOnSuccessListener { categories ->
                    for (category in categories) {
                        val categoryId = category.id
                        firestore.collection("users").document(userId).collection("stats")
                            .document("word_stats").collection("categories").document(categoryId)
                            .collection("words").whereEqualTo("pl", currentWord).get()
                            .addOnSuccessListener { words ->
                                for (wordDoc in words) {
                                    val wordRef = firestore.collection("users").document(userId)
                                        .collection("stats").document("word_stats").collection("categories")
                                        .document(categoryId).collection("words").document(wordDoc.id)

                                    firestore.runTransaction { transaction ->
                                        val snapshot = transaction.get(wordRef)
                                        val currentCorrectCount = snapshot.getLong("correctCount") ?: 0
                                        val currentMistakeCounter = snapshot.getLong("mistakeCounter") ?: 0
                                        if (isCorrect) {
                                            transaction.update(wordRef, "correctCount", currentCorrectCount + 1)
                                        } else {
                                            transaction.update(wordRef, "mistakeCounter", currentMistakeCounter + 1)
                                        }
                                        null
                                    }.addOnSuccessListener {
                                        Log.d("QuizFragment", "Word stats updated successfully")
                                    }.addOnFailureListener { exception ->
                                        Log.e("QuizFragment", "Error updating word stats", exception)
                                    }
                                }
                            }
                    }
                }
        }
    }

    private fun checkIfAnswerIsCorrect(selectedAnswer: String): Boolean {
        val currentWord = binding.QuestiontextView.text.toString()
        val storageRef = storage.reference.child("betterAnswers.json")
        storageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes)
                try {
                    val jsonObject = JSONObject(jsonString)
                    val categories = jsonObject.getJSONObject("categories")
                    categories.keys().forEach { categoryKey ->
                        val category = categories.getJSONObject(categoryKey)
                        val words = category.getJSONArray("words")
                        for (i in 0 until words.length()) {
                            val jsonWord = words.getJSONObject(i)
                            val wordPl = jsonWord.getString("pl")
                            if (wordPl == currentWord) {
                                val correctAnswer = jsonWord.getString("correctAnswer")
                                val isCorrect = selectedAnswer == correctAnswer
                                val selectedButton = when (selectedAnswer) {
                                    binding.buttonAnswer1.text.toString() -> binding.buttonAnswer1
                                    binding.buttonAnswer2.text.toString() -> binding.buttonAnswer2
                                    binding.buttonAnswer3.text.toString() -> binding.buttonAnswer3
                                    binding.buttonAnswer4.text.toString() -> binding.buttonAnswer4
                                    else -> null
                                }

                                selectedButton?.let {
                                    val backgroundDrawable = if (isCorrect) {
                                        R.drawable.correct_answer
                                    } else {
                                        R.drawable.wrong_answer
                                    }
                                    it.setBackgroundResource(backgroundDrawable)
                                }
                                updateAnswerIndicator(isCorrect)
                                Log.d("QuizFragment", "Is answer correct: $isCorrect, selected: $selectedAnswer ")
                                updateWordStats(isCorrect)
                                return@addOnSuccessListener
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QuizFragment", "Error parsing JSON", e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("QuizFragment", "Error loading JSON from storage", exception)
            }
        return false
    }

    private fun disableAnswerButtons() {
        binding.apply {
            buttonAnswer1.isEnabled = false
            buttonAnswer2.isEnabled = false
            buttonAnswer3.isEnabled = false
            buttonAnswer4.isEnabled = false
        }
    }

    private fun enableAnswerButtons() {
        binding.apply {
            buttonAnswer1.isEnabled = true
            buttonAnswer2.isEnabled = true
            buttonAnswer3.isEnabled = true
            buttonAnswer4.isEnabled = true
        }
    }
    private fun loadAnswersFromStorage(word: String) {
        val storageRef = storage.reference.child("betterAnswers.json")
        storageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes)
                try {
                    val jsonObject = JSONObject(jsonString)
                    val categories = jsonObject.getJSONObject("categories")
                    categories.keys().forEach { categoryKey ->
                        val category = categories.getJSONObject(categoryKey)
                        val words = category.getJSONArray("words")
                        for (i in 0 until words.length()) {
                            val jsonWord = words.getJSONObject(i)
                            val wordPl = jsonWord.getString("pl")
                            if (wordPl == word) {
                                val answers = jsonWord.getJSONArray("answers")
                                binding.apply {
                                    buttonAnswer1.text = answers.getString(0)
                                    buttonAnswer2.text = answers.getString(1)
                                    buttonAnswer3.text = answers.getString(2)
                                    buttonAnswer4.text = answers.getString(3)
                                }
                                // Odblokowujemy przyciski odpowiedzi
                                isAnswered = false
                                enableAnswerButtons()
                                return@addOnSuccessListener
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QuizFragment", "Error parsing JSON", e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("QuizFragment", "Error loading JSON from storage", exception)
            }
    }
}
