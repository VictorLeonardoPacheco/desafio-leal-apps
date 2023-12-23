package com.example.desafiolealapps.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivityMainBinding
import com.example.desafiolealapps.databinding.ActivityMyAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MyAccountActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyAccountBinding

    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        setupContentView()
    }

    private fun setupContentView() {
        setupLogoutButton()
        setupUserInformations()
    }

    private fun setupLogoutButton(){
        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            goToLoginActivity()
        }
    }

    private fun setupUserInformations(){
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid)
                .addSnapshotListener { document, error ->
                    if (document != null ){
                        binding.name.text = document.getString("name")
                        binding.age.text = document.getString("age")
                    }
                }
        }
    }

    private fun goToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}


