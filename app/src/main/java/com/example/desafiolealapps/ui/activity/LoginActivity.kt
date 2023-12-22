package com.example.desafiolealapps.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiolealapps.MainActivity
import com.example.desafiolealapps.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        setupContentView()
    }

    private fun setupContentView(){
        setupLoginButton()
        setupSignUpClick()
    }

    private fun setupLoginButton(){
        binding.signInButton.setOnClickListener{
            val email: String = binding.editTextUserEmail.text.toString()
            val password: String = binding.editTextUserNewPassword.text.toString()
            signInWithEmailAndPassword(email, password)
        }
    }

    private fun setupSignUpClick(){
        binding.signUpHere.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithEmailAndPassword(email:String, password: String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if (task.isSuccessful){
                Log.d(TAG, "signInWithEmailAndPassword: Sucess")
                val user = auth.currentUser
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Log.d(TAG, "signInWithEmailAndPassword: Fail", task.exception)
                Toast.makeText(baseContext, "Authentication Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        private var TAG = "EmailAndPassword"
    }
}