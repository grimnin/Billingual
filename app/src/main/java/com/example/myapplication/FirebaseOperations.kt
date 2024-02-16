
import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseOperations(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun addWordStatsSubcollection(userId: String) {
        // Collection reference for 'users' collection
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        val emptyData = hashMapOf<String, Any>()

        // Set the 'categories' subcollection within the 'word_stats'
        statsCollectionRef.document("word_stats").set(emptyData)
    }

    fun copyWordsToCategories(userId: String) {
        // Collection reference for 'users' collection
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        // Collection reference for 'word_stats' document in 'stats' subcollection
        val wordStatsDocRef = statsCollectionRef.document("word_stats")

        // Collection reference for 'categories' subcollection within 'word_stats'
        val categoriesCollectionRef = wordStatsDocRef.collection("categories")

        // Collection reference for 'words' collection
        val wordsCollectionRef = db.collection("words")

        // Query to get all documents from 'words' collection
        wordsCollectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Get the data from each document
                val data = document.data

                // Set the data in 'categories' collection within 'word_stats' document
                val categoryDocRef = categoriesCollectionRef.document(document.id)

                categoryDocRef.set(data).addOnSuccessListener {
                    // Copy subcollections
                    copySubcollections(document.reference, categoryDocRef)
                }.addOnFailureListener { exception ->
                    // Handle failure
                }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
        }
    }

    private fun copySubcollections(sourceDocRef: DocumentReference, targetDocRef: DocumentReference) {
        sourceDocRef.collection("words").get().addOnSuccessListener { subcollection ->
            for (subDoc in subcollection) {
                // Get the data from subcollection
                val subDocData = subDoc.data

                // Set the data in target document in 'categories' collection
                targetDocRef.collection("words").document(subDoc.id).set(subDocData)
                    .addOnSuccessListener {
                        // Handle successful copying of subcollection
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure in copying subcollection
                    }
            }
        }.addOnFailureListener { exception ->
            // Handle failure in getting subcollection
        }
    }

    /*fun updateUsersScore(userId: String) {
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        // Document reference for 'word_stats' document in 'stats' subcollection
        val wordStatsDocRef = statsCollectionRef.document("word_stats")

        // Collection reference for 'categories' subcollection within 'word_stats'
        val categoriesCollectionRef = wordStatsDocRef.collection("categories")

        // Collection reference for 'animals' document in 'categories' subcollection
        val animalsDocRef = categoriesCollectionRef.document("animals")

        // Collection reference for 'words' subcollection within 'animals'
        val wordsCollectionRef = animalsDocRef.collection("words")

        // Zmienna do przechowywania sumy wartości pola 'total' we wszystkich dokumentach
        var totalSum = 0L

        // Pobierz wszystkie dokumenty z podkolekcji 'words' w dokumencie 'animals'
        wordsCollectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Pobierz wartość pola 'total' z dokumentu
                val totalValue = document.getLong("total") ?: 0

                // Dodaj wartość do sumy
                totalSum += totalValue
            }

            // Aktualizuj pole 'score' w kolekcji 'users' na zsumowaną wartość
            userDocRef.update("score", totalSum)
                .addOnSuccessListener {
                    // Tutaj możesz dodać odpowiednią logikę lub powiadomienie
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd aktualizacji
                    // W praktyce warto dodać odpowiednie logi lub obsługę błędów
                }
        }.addOnFailureListener { exception ->
            // Obsłuż błąd pobierania dokumentów
            // W praktyce warto dodać odpowiednie logi lub obsługę błędów
        }
    }*/

}


