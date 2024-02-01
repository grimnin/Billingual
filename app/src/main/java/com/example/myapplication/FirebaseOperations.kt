import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseOperations(private val context: Context) {

    fun addCategories(userId: String, categories: List<String>) {
        val db = FirebaseFirestore.getInstance()

        val categoriesData = hashMapOf<String, Any>()

        for (category in categories) {
            val document = hashMapOf("id" to category)
            categoriesData[category] = document
        }

        val userStatsRef = db.collection("users").document(userId).collection("stats").document("word_stats")

        for ((category, document) in categoriesData) {
            userStatsRef.collection("categories").document(category).set(document)
                .addOnSuccessListener {
                    // Successfully added category document
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }
    }


}
