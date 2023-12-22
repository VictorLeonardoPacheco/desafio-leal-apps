package com.example.desafiolealapps.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupContentView()
    }

    private fun setupContentView(){
        setupNewPasswordInput()
        setupNewPasswordConfirmationInput()
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
        binding.changePasswordbutton.isEnabled = false

        if (!binding.editTextUserEmail.text.isNullOrBlank() && binding.editTextUserEmailLayout.error == null
            && !binding.editTextUserNewPassword.text.isNullOrBlank() && binding.editTextUserNewPasswordLayout.error == null
            && !binding.editTextUserNewPasswordConfirmation.text.isNullOrBlank() && binding.editTextUserNewPasswordConfirmationLayout.error == null
        ) {
            binding.changePasswordbutton.isEnabled = true
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

}