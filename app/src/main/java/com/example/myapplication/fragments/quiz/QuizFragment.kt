import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentQuizBinding
import com.example.myapplication.fragments.quiz.QuizViewModel
import com.example.myapplication.fragments.quiz.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject

class QuizFragment : Fragment() {

    private lateinit var binding: FragmentQuizBinding
    private lateinit var viewModel: QuizViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val storage = Firebase.storage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicjalizacja Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Inicjalizacja ViewModel
        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        // Pobierz i wyświetl losowe słowo dla quizu
        getRandomWordForQuiz()

        // Ustawienie onClickListenera dla przycisków odpowiedzi
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

            // Pobierz wszystkie słowa ze wszystkich kategorii
            firestore.collection("users").document(userId).collection("stats")
                .document("word_stats").collection("categories").get()
                .addOnSuccessListener { categories ->
                    val allWords = mutableListOf<Word>()
                    val categoryCount = categories.size() // Liczba kategorii
                    var categoriesProcessed = 0 // Licznik przetworzonych kategorii
                    for (category in categories) {
                        val categoryId = category.id
                        // Pobierz słowa dla każdej kategorii i dodaj je do listy wszystkich słów
                        firestore.collection("users").document(userId).collection("stats")
                            .document("word_stats").collection("categories").document(categoryId)
                            .collection("words").get()
                            .addOnSuccessListener { words ->
                                for (wordDoc in words) {
                                    val word = wordDoc.toObject(Word::class.java)
                                    allWords.add(word)
                                }
                                categoriesProcessed++
                                // Jeśli wszystkie kategorie zostały przetworzone, wybierz jedno losowe słowo spośród wszystkich słów
                                if (categoriesProcessed == categoryCount) {
                                    // Wybierz losowe słowo z pobranych wszystkich słów
                                    val randomWord = selectRandomWord(allWords)
                                    if (randomWord != null) {
                                        val wordEng = randomWord.pl
                                        binding.QuestiontextView.text = wordEng // Aktualizuj pole tekstowe
                                        loadAnswersFromStorage(randomWord.pl)
                                    } else {
                                        Log.d("QuizFragment", "Brak dostępnych słów")
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Obsłuż błąd pobierania słów
                                categoriesProcessed++
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd pobierania kategorii
                }
        }
    }

    private fun selectRandomWord(wordsList: List<Word>): Word? {
        // Stwórz listę słów, które mogą być wybrane na podstawie stosunku błędów do poprawnych odpowiedzi
        val selectableWords = mutableListOf<Word>()

        // Iteruj przez listę słów i dodaj do listy wszystkie słowa, które mają chociaż jedno wystąpienie (total > 0)
        for (word in wordsList) {
            if (word.total >= 0) {
                selectableWords.add(word)
            }
        }

        // Jeżeli lista wyboru jest pusta, zwróć null (brak dostępnych słów)
        if (selectableWords.isEmpty()) {
            return null
        }

        // Przygotuj listę wag dla każdego słowa na podstawie stosunku błędów do poprawnych odpowiedzi
        val weights = mutableListOf<Int>()
        for (word in selectableWords) {
            // Oblicz wagę na podstawie stosunku błędów do poprawnych odpowiedzi
            val errorCount = word.mistakeCounter+1
            val correctCount = word.correctCount+1
            val ratio = if (correctCount > 0) errorCount.toDouble() / correctCount.toDouble() else errorCount.toDouble() + 1 // Dodaj 1, aby uniknąć dzielenia przez zero
            val weight = (ratio * 10).toInt() + 1 // Pomnóż przez 10, aby uzyskać większe wartości wag
            repeat(weight) {
                weights.add(selectableWords.indexOf(word)) // Dodaj indeks słowa do listy wag
            }
        }

        // Wylosuj indeks na podstawie wag
        val randomIndex = (weights.indices).random()

        // Wyświetl wartości wylosowanego słowa
        val selectedWord = selectableWords[weights[randomIndex]]
        Log.d("QuizFragment", "Wylosowane słowo: ${selectedWord.eng}, mistakeCounter: ${selectedWord.mistakeCounter}, correctCount: ${selectedWord.correctCount}, total: ${selectedWord.total}")
        // Zwróć wybrane słowo
        return selectedWord
    }

    private fun checkAnswer(selectedAnswer: String) {
        // Tutaj możesz dodać logikę sprawdzania poprawności odpowiedzi
    }

    private fun loadAnswersFromStorage(word: String) {
        // Pobierz referencję do pliku JSON w Storage
        val storageRef = storage.reference.child("answers.json")

        // Pobierz plik JSON z Storage
        storageRef.getBytes(1024 * 1024) // Pobierz maksymalnie 1 MB danych
            .addOnSuccessListener { bytes ->
                val jsonString = String(bytes)
                try {
                    // Parsuj JSON
                    val jsonObject = JSONObject(jsonString)
                    val categories = jsonObject.getJSONObject("categories")

                    // Iteruj przez kategorie
                    categories.keys().forEach { categoryKey ->
                        val category = categories.getJSONObject(categoryKey)
                        val words = category.getJSONArray("words")

                        // Iteruj przez słowa w danej kategorii
                        for (i in 0 until words.length()) {
                            val jsonWord = words.getJSONObject(i)
                            val wordPl = jsonWord.getString("pl")
                            if (wordPl == word) {
                                // Jeśli słowo jest tym, które wylosowaliśmy, ustaw odpowiedzi na przyciskach
                                val answers = jsonWord.getJSONArray("answers")
                                binding.apply {
                                    buttonAnswer1.text = answers.getString(0)
                                    buttonAnswer2.text = answers.getString(1)
                                    buttonAnswer3.text = answers.getString(2)
                                    buttonAnswer4.text = answers.getString(3)
                                }
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
