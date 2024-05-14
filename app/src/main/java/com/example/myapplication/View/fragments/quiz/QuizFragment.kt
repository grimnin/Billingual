package com.example.myapplication.View.fragments.quiz

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.Model.data.Word
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuizBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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

                // Pobieranie wybranych kategorii z SharedPreferences
                val selectedCategories = sharedPreferences.getStringSet("selectedCategories", setOf())

                // Utworzenie zapytania do Firestore z filtrem na wybrane kategorie
                var query: Query = firestore.collection("users").document(userId).collection("stats")
                    .document("word_stats").collection("categories")
                if (selectedCategories != null && selectedCategories.isNotEmpty()) {
                    query = query.whereIn("name", selectedCategories.toList())

                }

                query.get()
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
                                            startCountdownTimer()
                                            attemptCount++

                                        } else {
                                            Log.d("com.example.myapplication.fragments.quiz.QuizFragment", "No available words")
                                        }

                                    }
                                }
                                .addOnFailureListener { exception ->
                                    categoriesProcessed++
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error getting categories", exception)
                    }
            } else {
                Log.d("com.example.myapplication.fragments.quiz.QuizFragment", "User has used all attempts")
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
        Log.d("com.example.myapplication.fragments.quiz.QuizFragment", "Random word: ${selectedWord.eng}, mistakeCounter: ${selectedWord.mistakeCounter}, correctCount: ${selectedWord.correctCount}, total: ${selectedWord.total}")
        return selectedWord
    }

    // Gdy użytkownik wybierze odpowiedź
    private fun checkAnswer(selectedAnswer: String) {
        if (!isAnswered) {
            isAnswered = true

            disableAnswerButtons()
            isCorrect = checkIfAnswerIsCorrect(selectedAnswer)
            Log.d("Boo", "$isCorrect")

            // Zatrzymaj odliczanie czasu
            countDownTimer.cancel()

            // Zmiana tła przycisku na podstawie poprawności odpowiedzi
            if (attemptCount < 5) {
                // Resetowanie tła przycisków po kilku sekundach
                Handler(Looper.getMainLooper()).postDelayed({
                    resetButtonBackgrounds()
                    getRandomWordForQuiz()
                }, 1000) // 2000 ms = 2 sekundy
            } else {
                goToMenuFragment()
            }
        }
    }

    private fun goToMenuFragment() {
        Handler(Looper.getMainLooper()).postDelayed({
            val fragmentManager = requireActivity().supportFragmentManager

            // Usuń fragment QuizFragment z kontenera
            val quizFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2)
            quizFragment?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }

            // Wyświetl MenuFragment w kontenerze
            val menuFragment = com.example.myapplication.View.fragments.MenuFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, menuFragment)
                .commit()
        }, 1000) // 2000 ms = 2 sekundy
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
                                        var madeMistake = snapshot.getBoolean("madeMistake") ?: false
                                        val currentTotal = snapshot.getLong("total") ?: 0
                                        if (!isCorrect) {
                                            madeMistake = true
                                        }
                                        if (madeMistake && currentCorrectCount >= currentMistakeCounter + 3) {
                                            madeMistake = false
                                        }
                                            updateScore(isCorrect)
                                        transaction.update(wordRef, "total", currentTotal + 1)
                                        transaction.update(wordRef, "madeMistake", madeMistake)
                                        if (isCorrect) {
                                            transaction.update(wordRef, "correctCount", currentCorrectCount + 1)
                                        } else {
                                            transaction.update(wordRef, "mistakeCounter", currentMistakeCounter + 1)
                                        }
                                        null
                                    }.addOnSuccessListener {
                                        Log.d("com.example.myapplication.fragments.quiz.QuizFragment", "Word stats updated successfully")
                                    }.addOnFailureListener { exception ->
                                        Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error updating word stats", exception)
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
                                Log.d("com.example.myapplication.fragments.quiz.QuizFragment", "Is answer correct: $isCorrect, selected: $selectedAnswer ")
                                updateWordStats(isCorrect)
                                return@addOnSuccessListener
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error parsing JSON", e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error loading JSON from storage", exception)
            }
        return false
    }
    private fun updateScore(isCorrect: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = firestore.collection("users").document(userId)

            // Atomowa operacja aktualizacji pola "score"
            userRef.update("score", FieldValue.increment(if (isCorrect) 1 else 0))
                .addOnSuccessListener {
                    Log.d(TAG, "User's score updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating user's score", e)
                }
        }
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
                                val shuffledAnswers = mutableListOf<String>()
                                for (j in 0 until answers.length()) {
                                    shuffledAnswers.add(answers.getString(j))
                                }
                                shuffledAnswers.shuffle() // Losowe rozmieszczenie odpowiedzi
                                binding.apply {
                                    buttonAnswer1.text = shuffledAnswers[0]
                                    buttonAnswer2.text = shuffledAnswers[1]
                                    buttonAnswer3.text = shuffledAnswers[2]
                                    buttonAnswer4.text = shuffledAnswers[3]
                                }
                                // Odblokowujemy przyciski odpowiedzi
                                isAnswered = false
                                enableAnswerButtons()
                                return@addOnSuccessListener
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error parsing JSON", e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("com.example.myapplication.fragments.quiz.QuizFragment", "Error loading JSON from storage", exception)
            }
    }

    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(5000, 1) { // Odliczanie z 5 sekund
            override fun onTick(millisUntilFinished: Long) {
                // Aktualizacja paska postępu, na przykład zmieniając jego szerokość
                val progress = millisUntilFinished.toFloat() / 5000f * 100f
                binding.progressBar.progress = progress.toInt()

            }

            override fun onFinish() {
                binding.progressBar.setProgress(0)
                // Jeśli odliczanie się skończyło, traktuj to jako udzielenie złej odpowiedzi
                if (!isAnswered) {
                    checkAnswer("") // Pusty ciąg oznacza brak odpowiedzi

                }
            }
        }.start()
    }


}