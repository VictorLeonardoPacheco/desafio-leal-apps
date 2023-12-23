package com.example.desafiolealapps.ui.activities

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiolealapps.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {
    private val REQ_ONE_TAP = 2
    lateinit var binding: ActivityLoginBinding
    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest

    private lateinit var auth: FirebaseAuth

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        Log.d(TAG, "Got ID token.")
                        val firebaseAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseAuthCredential).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Log.d(TAG, "signInWithCredential: Success")
                                goToMainActivity()
                            } else {
                                Log.d(TAG, "signInWithCredential: Failure", task.exception)
                            }
                        }
                    } else -> {
                        Log.d(TAG, "No id token!")
                    }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            goToMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("381814208255-u0lete2pela8k4jgs7q7kg0jbg8io6vp.apps.googleusercontent.com")
                    .build()
            ).setAutoSelectEnabled(true).build()

        setupContentView()
    }

    private fun setupContentView() {
        setupLoginButton()
        setupSignUpClick()
        setupSignInGoogle()
    }

    private fun setupSignInGoogle() {
        binding.buttonSignInWithGoogle.setOnClickListener {
            showAuthGoogle()
        }
    }

    private fun showAuthGoogle() {
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
            try {
                startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    REQ_ONE_TAP,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: IntentSender.SendIntentException) {
                Log.e(TAG, "Coudn't start One Tap UI")
            }
        }.addOnFailureListener(this) { e ->
            e.localizedMessage?.let { Log.d(TAG, it) }
        }
    }


    private fun setupLoginButton() {
        binding.signInButton.setOnClickListener {
            val email: String = binding.editTextUserEmail.text.toString()
            val password: String = binding.editTextUserNewPassword.text.toString()
            signInWithEmailAndPassword(email, password)
        }
    }

    private fun setupSignUpClick() {
        binding.signUpHere.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }


    private fun goToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmailAndPassword: Sucess")
                val user = auth.currentUser
                goToMainActivity()
            } else {
                Log.d(TAG, "signInWithEmailAndPassword: Fail", task.exception)
                Toast.makeText(baseContext, "Authentication Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private var TAG = "EmailAndPassword"
    }
}