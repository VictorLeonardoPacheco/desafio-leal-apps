package com.example.desafiolealapps.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.desafiolealapps.data.repositories.AuthenticationFirebaseRepository
import com.example.desafiolealapps.ui.states.LoginUIState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow


class AuthViewModel(private val db: AuthenticationFirebaseRepository): ViewModel(){

    @Suppress("UNCHECKED_CAST")
    companion object {
        val Factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            val repo = AuthenticationFirebaseRepository(Firebase.auth)
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return AuthViewModel(repo) as T
            }
        }
    }
}