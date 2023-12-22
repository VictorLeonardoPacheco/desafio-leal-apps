package com.example.desafiolealapps.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivityLoginBinding
import com.example.desafiolealapps.databinding.ActivitySignUpBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupContentView()
    }

    private fun setupContentView(){
    }

}