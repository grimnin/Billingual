package com.example.myapplication.Model.repository
import android.content.Context
import android.util.Log
import com.example.myapplication.Model.data.IrregularVerb
import com.example.myapplication.View.fragments.grammar.Sentence
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseOperations(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private   val user = auth.currentUser
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
                    copySubcollections(document.reference, categoryDocRef, "words")
                }.addOnFailureListener { exception ->
                    // Handle failure
                }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
        }
    }

    private fun copySubcollections(
        sourceDocRef: DocumentReference,
        targetDocRef: DocumentReference,
        collectionPath: String
    ) {
        sourceDocRef.collection(collectionPath).get().addOnSuccessListener { subcollection ->
            for (subDoc in subcollection) {
                // Get the data from subcollection
                val subDocData = subDoc.data

                // Set the data in target document in 'categories' collection
                targetDocRef.collection(collectionPath).document(subDoc.id).set(subDocData)
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

    fun addGrammarStatsDocument(userId: String) {
        // Collection reference for 'users' collection
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        val emptyData = hashMapOf<String, Any>()

        // Set the 'grammar_stats' document within the 'stats' subcollection
        statsCollectionRef.document("grammar_stats").set(emptyData)
    }

    fun copyGrammarCollection(userId: String) {
        // Collection reference for 'users' collection
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        // Document reference for 'grammar_stats' document in 'stats' subcollection
        val grammarStatsDocRef = statsCollectionRef.document("grammar_stats")

        // Collection reference for 'grammar' collection
        val grammarCollectionRef = db.collection("grammar")

        // Query to get all documents from 'grammar' collection
        grammarCollectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Get the data from each document
                val data = document.data

                // Set the data in 'grammar_stats' document
                grammarStatsDocRef.collection("grammar").document(document.id).set(data)
                    .addOnSuccessListener {
                        // Handle successful copying of document
                        // If there are subcollections, you can call a function to copy them
                        copySubcollections(
                            document.reference,
                            grammarStatsDocRef.collection("grammar").document(document.id),
                            "verbs"
                        )
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
        }
    }

    fun getRandomVerbs(userId: String, callback: (List<IrregularVerb>) -> Unit) {
        val userDocRef = db.collection("users").document(userId)
        val grammarStatsDocRef = userDocRef.collection("stats")
            .document("grammar_stats")
            .collection("grammar")
            .document("irregular_verbs")
            .collection("verbs")

        grammarStatsDocRef.get().addOnSuccessListener { documents ->
            val verbsList = mutableListOf<IrregularVerb>()
            for (document in documents) {
                val base = document.getString("Basic") ?: ""
                val pastSimple = document.getString("PastSimple") ?: ""
                val pastPerfect = document.getString("PastPerfect") ?: ""
                val pl = document.getString("pl") ?: ""
                val id = document.getString("id") ?: ""
                val verb = IrregularVerb(base, pastSimple, pastPerfect, pl, id, 0, 0, false)
                verbsList.add(verb)
            }
            val randomVerbs = verbsList.shuffled().take(5)
            callback(randomVerbs)
        }.addOnFailureListener { exception ->
            // Handle failure
            callback(emptyList())
        }
    }

    fun updateUserScore(userId: String, additionalScore: Int) {
        val userDocRef = db.collection("users").document(userId)

        // Get current score from Firestore
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentScore = documentSnapshot.getLong("score") ?: 0
                    val newScore = currentScore.toInt() + additionalScore

                    // Update user's score field in Firestore with the new score
                    userDocRef.update("score", newScore)
                        .addOnSuccessListener {
                            // Handle success, if needed
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                            Log.e("FirebaseOperations", "Failed to update score: $exception")
                        }
                } else {
                    Log.e("FirebaseOperations", "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.e("FirebaseOperations", "Failed to get user document: $exception")
            }
    }

    fun updateVerbStats(userId: String, verbId: String, isCorrect: Boolean) {
        val userDocRef = db.collection("users").document(userId)
        val verbDocRef = userDocRef.collection("stats")
            .document("grammar_stats")
            .collection("grammar")
            .document("irregular_verbs")
            .collection("verbs")
            .document(verbId)

        db.runTransaction { transaction ->
            val docSnapshot = transaction.get(verbDocRef)

            // Get the stats map from the document
            val statsMap = docSnapshot.get("stats") as? Map<String, Any>

            // Increment the correctAnswers or wrongAnswers based on the answer correctness
            val updatedStatsMap = statsMap?.toMutableMap()
            updatedStatsMap?.let {
                val currentCorrectAnswers = it["correctAnswers"] as? Long ?: 0
                val currentWrongAnswers = it["wrongAnswers"] as? Long ?: 0
                val updatedMadeMistake =
                    if (currentCorrectAnswers >= currentWrongAnswers + 1 && it["madeMistake"] == true) false else !isCorrect || (it["madeMistake"] as? Boolean) ?: false

                // Update correctAnswers or wrongAnswers based on the answer correctness
                if (isCorrect) {
                    it["correctAnswers"] = currentCorrectAnswers + 1
                } else {
                    it["wrongAnswers"] = currentWrongAnswers + 1
                }
                // Update madeMistake
                it["madeMistake"] = updatedMadeMistake

                // Update the stats map in the document
                transaction.update(verbDocRef, "stats", updatedStatsMap)
            }

            null
        }.addOnSuccessListener {
            Log.d("FirebaseOperations", "Transaction successfully updated.")
        }.addOnFailureListener { e ->
            Log.w("FirebaseOperations", "Transaction failure.", e)
        }
    }


    fun updateVerbMistakeStatus(verbId: String, newValue: Boolean) {

        user?.let { currentUser ->
            val userId = currentUser.uid
            val verbDocRef = db.collection("users").document(userId)
                .collection("stats").document("grammar_stats")
                .collection("grammar").document("irregular_verbs")
                .collection("verbs").document(verbId)

            verbDocRef.update("stats.madeMistake", newValue)
                .addOnSuccessListener {
                    Log.d("FirebaseOperations", "Verb mistake status updated successfully.")
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseOperations", "Error updating verb mistake status", exception)
                }
        }


    }

    fun deleteWordForAllUsers(category: String, wordId: String) {
        // Get reference to the word document to be deleted
        val wordDocRef = db.collection("words").document(category)
            .collection("words").document(wordId)

        // Get all users
        db.collection("users").get()
            .addOnSuccessListener { users ->
                for (user in users) {
                    val userId = user.id
                    // Get reference to the user's document
                    val userDocRef = db.collection("users").document(userId)
                        .collection("stats").document("word_stats")
                        .collection("categories").document(category)
                        .collection("words").document(wordId)


                    // Delete the word document for the user
                    userDocRef.delete()
                        .addOnSuccessListener {
                            Log.d("FirebaseOperations", "Word deleted for user: $userId and wordID is $wordId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseOperations", "Error deleting word for user: $userId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error getting users for word deletion", e)
            }

        // Delete the word document from the main 'words' collection
        wordDocRef.delete()
            .addOnSuccessListener {
                Log.d("FirebaseOperations", "Word deleted from main collection")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error deleting word from main collection", e)
            }
    }


    fun deleteVerbForAllUsers(verbToDelete: String) {
        // Collection reference for 'users' collection
        val usersCollectionRef = db.collection("users")

        // Get all users
        usersCollectionRef.get()
            .addOnSuccessListener { users ->
                for (user in users) {
                    val userId = user.id

                    // Document reference for the verb to delete for the current user
                    val verbDocRef = db.collection("users").document(userId)
                        .collection("stats").document("grammar_stats")
                        .collection("grammar").document("irregular_verbs")
                        .collection("verbs").whereEqualTo("pl", verbToDelete)



                    // Delete the verb document for the user
                    verbDocRef.get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        Log.d("FirebaseOperations", "Verb deleted successfully for user: $userId")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirebaseOperations", "Error deleting verb for user: $userId", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseOperations", "Error getting verb document for user: $userId", e)
                        }

                }
                val verbDocRef2=db.collection("grammar").document("irregular_verbs").collection("verbs").whereEqualTo("pl", verbToDelete)
                verbDocRef2.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener { e ->

                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseOperations", "Error getting verb document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error getting users for deleting verbs", e)
            }
    }

    fun copyGrammarSentences(userId: String) {
        // Collection reference for 'users' collection
        val userDocRef = db.collection("users").document(userId)

        // Collection reference for 'stats' subcollection
        val statsCollectionRef = userDocRef.collection("stats")

        // Document reference for 'grammar_stats' document in 'stats' subcollection
        val grammarStatsDocRef = statsCollectionRef.document("grammar_stats")

        // Collection reference for 'grammar' collection within 'grammar_stats'
        val grammarCollectionRef = grammarStatsDocRef.collection("grammar")

        // Collection reference for 'tenses' subcollection within 'grammar'
        val tensesCollectionRef = grammarCollectionRef.document("tenses")
            .collection("sentences")

        // Collection reference for '/grammar/tenses/sentences' subcollection
        val sourceSentencesCollectionRef = db.collection("grammar").document("tenses")
            .collection("sentences")

        // Get all documents from '/grammar/tenses/sentences' collection
        sourceSentencesCollectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                // Get the data from each document
                val data = document.data

                // Set the data in 'sentences' collection within 'grammar' document
                val sentenceDocRef = tensesCollectionRef.document(document.id)
                sentenceDocRef.set(data)
                    .addOnSuccessListener {
                        // Handle successful copying of sentence
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure in copying sentence
                    }
            }
        }.addOnFailureListener { exception ->
            // Handle failure in getting sentences
        }
    }

    fun deleteSentenceForAllUsers(sentenceId: String) {
        val db = FirebaseFirestore.getInstance()

        // Reference to the 'users' collection
        val usersCollectionRef = db.collection("users")

        // Reference to the 'grammar/tenses/sentences' collection
        val sentencesCollectionRef = db.collection("grammar")
            .document("tenses")
            .collection("sentences")

        // Delete the sentence document from 'grammar/tenses/sentences'
        sentencesCollectionRef.document(sentenceId).delete()
            .addOnSuccessListener {
                Log.d("FirebaseOperations", "Sentence deleted from 'grammar/tenses/sentences'")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error deleting sentence from 'grammar/tenses/sentences'", e)
            }

        // Get all users
        usersCollectionRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.id

                    // Reference to the sentences collection for the user
                    val sentencesRef = usersCollectionRef
                        .document(userId)
                        .collection("stats")
                        .document("grammar_stats")
                        .collection("grammar")
                        .document("tenses")
                        .collection("sentences")

                    // Delete the sentence document for the user
                    sentencesRef.document(sentenceId).delete()
                        .addOnSuccessListener {
                            Log.d("FirebaseOperations", "Sentence deleted for user: $userId")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseOperations", "Error deleting sentence for user: $userId", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseOperations", "Error getting users for deleting sentence", exception)
            }
    }


    fun getRandomSentences(callback: (List<Sentence>) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userDocRef = db.collection("users").document(userId)
            val grammarStatsDocRef = userDocRef.collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")
                .collection("sentences") // Access the "sentences" subcollection

            grammarStatsDocRef.get().addOnSuccessListener { documents ->
                val sentences = mutableListOf<Sentence>()
                val randomDocuments = documents.shuffled().take(3) // Get three random documents
                for (document in randomDocuments) {
                    val data = document.data
                    val sentence = data["sentence"] as? String ?: ""
                    val zdanie = data["zdanie"] as? String ?: ""
                    val tense = data["tense"] as? String ?: ""
                    val id = document.id
                    val correctAnswers = (data["correctAnswers"] as? Long)?.toInt() ?: 0
                    val wrongAnswers = (data["wrongAnswers"] as? Long)?.toInt() ?: 0
                    val madeMistake = data["madeMistake"] as? Boolean ?: false

                    val sentenceObject = Sentence(sentence, zdanie, tense, id, correctAnswers, wrongAnswers, madeMistake)
                    sentences.add(sentenceObject)
                }
                callback(sentences)
            }.addOnFailureListener { exception ->
                // Handle failure
                callback(emptyList())
            }
        } else {
            // User is not logged in
            callback(emptyList())
        }


    }

    fun updateSentenceStats(sentences: List<Sentence>, answers: List<String>) {
        val userDocRef = db.collection("users").document(user?.uid.toString())
        val grammarStatsDocRef = userDocRef.collection("stats")
            .document("grammar_stats")
            .collection("grammar")
            .document("tenses")
            .collection("sentences")

        for ((index, sentence) in sentences.withIndex()) {
            val answer = answers[index]
            val isCorrect = answer.equals(sentence.sentence, ignoreCase = true)

            db.runTransaction { transaction ->
                val sentenceDocRef = grammarStatsDocRef.document(sentence.id)

                // Update correctAnswers field
                if (isCorrect) {
                    val currentCorrectAnswers = sentence.correctAnswers
                    transaction.update(sentenceDocRef, "correctAnswers", currentCorrectAnswers + 1)
                }

                // Update wrongAnswers field and madeMistake field
                val currentWrongAnswers = sentence.wrongAnswers
                val updatedWrongAnswers = currentWrongAnswers + if (!isCorrect) 1 else 0
                val updatedMadeMistake = !isCorrect || sentence.madeMistake
                transaction.update(sentenceDocRef, "wrongAnswers", updatedWrongAnswers)
                transaction.update(sentenceDocRef, "madeMistake", updatedMadeMistake)

                null
            }.addOnSuccessListener {
                Log.d("FirebaseOperations", "Sentence stats updated successfully.")
            }.addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error updating sentence stats", e)
            }
        }
    }



    fun getSentencesWithMistakes(callback: (List<Sentence>) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userDocRef = db.collection("users").document(userId)
            val grammarStatsDocRef = userDocRef.collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")
                .collection("sentences") // Access the "sentences" subcollection

            grammarStatsDocRef.whereEqualTo("madeMistake", true).get().addOnSuccessListener { documents ->
                val sentences = mutableListOf<Sentence>()
                for (document in documents) {
                    val data = document.data
                    val sentence = data["sentence"] as? String ?: ""
                    val zdanie = data["zdanie"] as? String ?: ""
                    val tense = data["tense"] as? String ?: ""
                    val id = document.id
                    val correctAnswers = (data["correctAnswers"] as? Long)?.toInt() ?: 0
                    val wrongAnswers = (data["wrongAnswers"] as? Long)?.toInt() ?: 0
                    val madeMistake = data["madeMistake"] as? Boolean ?: false

                    val sentenceObject = Sentence(sentence, zdanie, tense, id, correctAnswers, wrongAnswers, madeMistake)
                    sentences.add(sentenceObject)
                }
                callback(sentences)
            }.addOnFailureListener { exception ->
                // Handle failure
                callback(emptyList())
            }
        } else {
            // User is not logged in
            callback(emptyList())
        }
    }

    fun addSentenceForAllUsers(sentence: String, zdanie: String, tense: String, id: String) {
        // Get reference to the 'users' collection
        val usersCollectionRef = db.collection("users")

        // Get reference to the 'grammar/tenses/sentences' collection
        val sentencesCollectionRef = db.collection("grammar")
            .document("tenses")
            .collection("sentences")

        // Create data for the sentence
        val sentenceData = hashMapOf(
            "sentence" to sentence,
            "zdanie" to zdanie,
            "tense" to tense,
            "id" to id,
            "correctAnswers" to 0,
            "wrongAnswers" to 0,
            "madeMistake" to false
        )

        // Add sentence to 'grammar/tenses/sentences' collection
        sentencesCollectionRef.document(id)
            .set(sentenceData)
            .addOnSuccessListener {
                Log.d("FirebaseOperations", "Sentence added to 'grammar/tenses/sentences'")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error adding sentence to 'grammar/tenses/sentences'", e)
            }

        // Get all users
        usersCollectionRef.get()
            .addOnSuccessListener { users ->
                for (user in users) {
                    val userId = user.id

                    // Document reference for the 'grammar_stats' document for the current user
                    val grammarStatsDocRef = db.collection("users").document(userId)
                        .collection("stats").document("grammar_stats")
                        .collection("grammar").document("tenses")
                        .collection("sentences").document(id)

                    // Set the data in the document
                    grammarStatsDocRef.set(sentenceData)
                        .addOnSuccessListener {
                            Log.d("FirebaseOperations", "Sentence added for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseOperations", "Error adding sentence for user: $userId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOperations", "Error getting users for adding sentence", e)
            }
    }

    fun updateMistakeSentenceStatus(sentenceId: String) {
        // Pobierz aktualnie zalogowanego użytkownika
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Sprawdź, czy użytkownik jest zalogowany
        currentUser?.let { user ->
            // Pobierz identyfikator aktualnie zalogowanego użytkownika
            val userId = user.uid

            // Odniesienie do kolekcji "sentences" w bazie danych
            val sentencesCollectionRef = db.collection("users")
                .document(userId)
                .collection("stats")
                .document("grammar_stats")
                .collection("grammar")
                .document("tenses")
                .collection("sentences")

            // Odniesienie do konkretnego dokumentu reprezentującego zdanie
            val sentenceDocRef = sentencesCollectionRef.document(sentenceId)

            // Aktualizacja pola "madeMistake" na wartość false
            sentenceDocRef.update("madeMistake", false)
                .addOnSuccessListener {
                    // Obsługa sukcesu
                    Log.d("FirebaseOperations", "Mistake status updated for sentence: $sentenceId")
                }
                .addOnFailureListener { e ->
                    // Obsługa błędu
                    Log.e("FirebaseOperations", "Error updating mistake status for sentence: $sentenceId", e)
                }
        }
    }


}




