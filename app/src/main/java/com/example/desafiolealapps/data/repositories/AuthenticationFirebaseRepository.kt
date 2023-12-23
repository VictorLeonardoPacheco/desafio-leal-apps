package com.example.desafiolealapps.data.repositories


import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthenticationFirebaseRepository(private val auth: FirebaseAuth) {

    private fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        }
    }

}