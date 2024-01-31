
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseOperations(private val context: Context) {




    class FirebaseOperations {
        private val db = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()

        public fun addCategories() {
            val user = auth.currentUser

            if (user != null) {
                // Pobierz kategorie z kolekcji 'words'
                db.collection("words")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val categories = mutableListOf<String>()

                        for (document in querySnapshot.documents) {
                            // Dla każdej kategorii w kolekcji 'words', dodaj ją do listy 'categories'
                            val category = document.id
                            categories.add(category)

                            // Dodaj podkolekcję 'stats' w kolekcji 'users' dla danego użytkownika
                            // z dokumentem 'stats' o id 'stats'
                            db.collection("users")
                                .document(user.uid)
                                .collection("stats")
                                .document("stats")
                                .set(mapOf("categories" to categories))
                                .addOnSuccessListener {
                                    println("Categories added successfully.")
                                }
                                .addOnFailureListener { e ->
                                    println("Error adding categories: $e")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error getting categories: $e")
                    }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
