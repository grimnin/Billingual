package com.example.myapplication
import android.content.Context
import android.util.Log
import com.example.myapplication.fragments.grammar.IrregularVerb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseOperations(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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
                    copySubcollections(document.reference, categoryDocRef,"words")
                }.addOnFailureListener { exception ->
                    // Handle failure
                }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
        }
    }

    private fun copySubcollections(sourceDocRef: DocumentReference, targetDocRef: DocumentReference,collectionPath:String) {
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
                        copySubcollections(document.reference, grammarStatsDocRef.collection("grammar").document(document.id),"verbs")
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
                val verb = IrregularVerb(base, pastSimple, pastPerfect, pl,id)
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
                val updatedMadeMistake = if (currentCorrectAnswers >= currentWrongAnswers + 1 && it["madeMistake"] == true) false else !isCorrect || (it["madeMistake"] as? Boolean) ?: false

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







}




