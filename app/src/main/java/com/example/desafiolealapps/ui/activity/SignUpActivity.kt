package com.example.desafiolealapps.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        setupContentView()
    }

    private fun setupContentView(){
        setupNewPasswordInput()
        setupNewPasswordConfirmationInput()
        setupSignInButton()
    }
    private fun setupSignInButton(){
        binding.signInButton.setOnClickListener{
            val email: String = binding.editTextUserEmail.text.toString()
            val password: String = binding.editTextUserNewPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()){
                createUserWithEmailAndPassword(email, password)
            }
        }
    }

    private fun createUserWithEmailAndPassword(email:String, password: String){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if (task.isSuccessful){
                Log.d(TAG, "createUserWithEmailAndPassword: Sucess")
                val user = auth.currentUser
            } else {
                Log.d(TAG, "createUserWithEmailAndPassword: Fail", task.exception)
                Toast.makeText(baseContext, "Authentication Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setupNewPasswordInput() {
        binding.editTextUserNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString().let { password ->
                    var passwordError = false

                    if (password.isNullOrBlank()) {
                        binding.editTextUserNewPasswordLayout.error =
                            getString(R.string.sign_up_error_password_blank)
                    }

                    if (password?.contains(Regex("[a-z]")) == true) {
                        binding.userPasswordRequirement1.setTextColor(
                            resources.getColor(R.color.verde)
                        )
                    } else {
                        binding.userPasswordRequirement1.setTextColor(
                            resources.getColor(R.color.cinza)
                        )
                        passwordError = true
                    }

                    if (password?.contains(Regex("[A-Z]")) == true) {
                        binding.userPasswordRequirement2.setTextColor(
                            resources.getColor(R.color.verde)
                        )
                    } else {
                        binding.userPasswordRequirement2.setTextColor(
                            resources.getColor(R.color.cinza)
                        )
                        passwordError = true
                    }

                    if (password?.contains(Regex("[0-9]")) == true) {
                        binding.userPasswordRequirement3.setTextColor(
                            resources.getColor(R.color.verde)
                        )
                    } else {
                        binding.userPasswordRequirement3.setTextColor(
                            resources.getColor(R.color.cinza)
                        )
                        passwordError = true
                    }

                    if ((password?.length ?: 0) >= 8) {
                        binding.userPasswordRequirement4.setTextColor(
                            resources.getColor(R.color.verde)
                        )
                    } else {
                        binding.userPasswordRequirement4.setTextColor(
                            resources.getColor(R.color.cinza)
                        )
                        passwordError = true
                    }

                    if (passwordError) {
                        binding.editTextUserNewPasswordLayout.error =
                            getString(R.string.sign_up_error_password_invalid)
                    } else {
                        binding.editTextUserNewPasswordLayout.error = null
                    }
                }
                enableChangePasswordButton()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun enableChangePasswordButton() {
        binding.signInButton.isEnabled = false

        if (!binding.editTextUserEmail.text.isNullOrBlank() && binding.editTextUserEmailLayout.error == null
            && !binding.editTextUserNewPassword.text.isNullOrBlank() && binding.editTextUserNewPasswordLayout.error == null
            && !binding.editTextUserNewPasswordConfirmation.text.isNullOrBlank() && binding.editTextUserNewPasswordConfirmationLayout.error == null
        ) {
            binding.signInButton.isEnabled = true
        }
    }

    private fun setupNewPasswordConfirmationInput() {
        binding.editTextUserNewPasswordConfirmation.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString().let { passwordConfirmation ->
                    var passwordConfirmationError = false

                    if (passwordConfirmation.isNullOrBlank()) {
                        binding.editTextUserNewPasswordConfirmationLayout.error =
                            getString(R.string.sign_up_error_password_blank)
                    }

                    if (passwordConfirmation != binding.editTextUserNewPassword.text.toString()) {
                        passwordConfirmationError = true
                    }

                    if (passwordConfirmationError) {
                        binding.editTextUserNewPasswordConfirmationLayout.error =
                            getString(R.string.sign_up_error_password_invalid)
                    } else {
                        binding.editTextUserNewPasswordConfirmationLayout.error = null
                    }

                }
                enableChangePasswordButton()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object{
        private var TAG = "EmailAndPassword"
    }

}