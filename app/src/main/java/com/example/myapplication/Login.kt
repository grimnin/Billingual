package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R.layout.login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignUp:TextView
    private lateinit var client:GoogleSignInClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(login)
        auth = Firebase.auth

        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client= GoogleSignIn.getClient(this,gso)


        val registerText:TextView=findViewById(R.id.textView_create_now)
        registerText.setOnClickListener {
            val intent= Intent(this,Register::class.java)
            startActivity(intent)
        }
        val googleSignUp:TextView=findViewById(R.id.textViewGoogleL)
        googleSignUp.setOnClickListener {
            //client.signOut()
            signInGoogle()
        }

        //Starting development of logging process
        val loginButton:Button=findViewById(R.id.buttonLoginL)
        loginButton.setOnClickListener {
            preformLogin()

        }


    }

    private fun signInGoogle() {
        val signIntent=client.signInIntent
        launcher.launch(signIntent)
    }

    private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
       result ->
        if(result.resultCode==Activity.RESULT_OK){
            val task=GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account:GoogleSignInAccount?=task.result
            if(account!=null){
                updateUI(account)
            }
        }
        else{
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential=GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){

                val intent:Intent=Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else{
                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
            }
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