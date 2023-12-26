package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R.layout.login
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(login)
        auth = Firebase.auth

        val registerText:TextView=findViewById(R.id.textView_create_now)
        registerText.setOnClickListener {
            val intent= Intent(this,Register::class.java)
            startActivity(intent)
        }

        //Starting development of logging process
        val loginButton:Button=findViewById(R.id.buttonLoginL)
        loginButton.setOnClickListener {
            preformLogin()

        }


    }

    private fun preformLogin() {
        val email=findViewById<EditText>(R.id.editTextLoginL)
        val password=findViewById<EditText>(R.id.editTextPasswordL)
        if(email.text.toString().isEmpty()||password.text.toString().isEmpty()){
            Toast.makeText(this,"Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val inputEmail=email.text.toString()
        val inputPassword=password.text.toString()

        auth.signInWithEmailAndPassword(inputEmail, inputPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful&& auth.currentUser?.isEmailVerified==true) {
                    // Sign in success, navigate to main activity
                    val intent=Intent(this,MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    Toast.makeText(
                        baseContext, "Success.",
                        Toast.LENGTH_SHORT,).show()

                }
                else if(auth.currentUser?.isEmailVerified==false){
                    Toast.makeText(
                        baseContext, "You haven't verify your account via mailbox yet.",
                        Toast.LENGTH_LONG,).show()
                }

                else {
                    // If sign in fails, display a message to the user.

                    Toast.makeText(baseContext,"Authentication failed.",
                        Toast.LENGTH_SHORT,).show()

                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext,"Authentication failed. ${it.localizedMessage}",
                    Toast.LENGTH_SHORT,).show()
            }



    }


}