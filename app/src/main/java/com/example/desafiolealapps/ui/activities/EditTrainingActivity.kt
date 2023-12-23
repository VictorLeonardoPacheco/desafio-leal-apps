package com.example.desafiolealapps.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.desafiolealapps.databinding.ActivityEditTrainingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class EditTrainingActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditTrainingBinding
    lateinit var trainingId: String
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        trainingId = intent.getStringExtra("trainingId").toString()
        setupContentView()
        val trainingName = intent.getStringExtra("trainingName")
        val trainingDescription = intent.getStringExtra("trainingDescription")
        val trainingTime = intent.getStringExtra("trainingTime")

        binding.editTextTrainingName.setText(trainingName)
        binding.editTextTrainingTime.setText(trainingTime)
        binding.editTextTrainingDescription.setText(trainingDescription)
    }

    private fun setupContentView() {
        if (intent.getStringExtra("trainingId") != null) {
            setupUpdateTrainingButton()
            setupDeleteButton()
        } else {
            binding.delete.visibility = View.GONE
            setupCreateTrainingButton()
        }
    }

    private fun setupDeleteButton() {
        binding.delete.visibility = View.VISIBLE
        binding.delete.setOnClickListener {
            val trainingId = intent.getStringExtra("trainingId") ?: return@setOnClickListener
            val currentUser = auth.currentUser

            currentUser?.uid?.let { userId ->
                deleteTraining(userId, trainingId)
            } ?: run {
                Log.e("EditTrainingActivity", "User is null")
            }
        }
    }

    private fun deleteTraining(userId: String, trainingId: String) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .document(trainingId)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("db", "Success deleting training! Document ID: $trainingId")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("db", "Error deleting training", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error deleting training", exception)
            }
    }

    private fun setupUpdateTrainingButton() {
        binding.createTrainingButton.setOnClickListener {
            val trainingId = intent.getStringExtra("trainingId") ?: return@setOnClickListener
            val trainingName = binding.editTextTrainingName.text.toString()
            val trainingDescription = binding.editTextTrainingDescription.text.toString()
            val trainingTime = binding.editTextTrainingTime.text.toString()

            val currentUser = auth.currentUser
            currentUser?.uid?.let { userId ->
                val trainingMap = hashMapOf(
                    "trainingName" to trainingName,
                    "trainingDescription" to trainingDescription,
                    "trainingTime" to trainingTime
                )

                updateTraining(userId, trainingId, trainingMap)
            } ?: run {
                Log.e("EditTrainingActivity", "User is null")
            }
        }
    }

    private fun updateTraining(userId: String, trainingId: String, trainingMap: Map<String, Any>) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .document(trainingId)
            .set(trainingMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("db", "Success updating training! Document ID: $trainingId")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("db", "Error updating training", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error updating training", exception)
            }
    }

    private fun setupCreateTrainingButton() {
        binding.createTrainingButton.setOnClickListener {
            val trainingName = binding.editTextTrainingName.text.toString()
            val trainingDescription = binding.editTextTrainingDescription.text.toString()
            val trainingTime = binding.editTextTrainingTime.text.toString()

            val currentUser = auth.currentUser
            currentUser?.uid?.let { userId ->
                val trainingMap = hashMapOf(
                    "trainingName" to trainingName,
                    "trainingDescription" to trainingDescription,
                    "trainingTime" to trainingTime
                )

                createTraining(userId, trainingMap)
            } ?: run {
                Log.e("EditTrainingActivity", "User is null")
            }
        }
    }

    private fun createTraining(userId: String, trainingMap: Map<String, Any>) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .add(trainingMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newTrainingId = task.result?.id
                    Log.d("db", "Success creating training! Document ID: $newTrainingId")

                    if (newTrainingId != null) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("db", "Error: Document ID is null")
                    }
                } else {
                    Log.e("db", "Error creating training", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error creating training", exception)
            }
    }
}
