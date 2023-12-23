package com.example.desafiolealapps.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivityCreateExerciceBinding
import com.example.desafiolealapps.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class CreateExerciceActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateExerciceBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExerciceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupContentView()
    }

    private fun setupContentView(){
        setupMyAccountButton()
    }

    private fun setupMyAccountButton(){
//        binding.myAccount.setOnClickListener {
//            goToMyAccountActivity()
//        }
    }

    private fun goToMyAccountActivity(){
        val intent = Intent(this, MyAccountActivity::class.java)
        startActivity(intent)
    }


}