package com.example.desafiolealapps.ui.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.desafiolealapps.R
import com.example.desafiolealapps.databinding.ActivityCreateExerciseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CreateExerciseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateExerciseBinding
    private lateinit var trainingId: String
    private lateinit var exerciseId: String
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val imageName = System.currentTimeMillis().toString() + ".jpg"
    private val imageRef = storageRef.child("images/$imageName")
    private var exerciseImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        trainingId = intent.getStringExtra("trainingId").toString()
        exerciseId = intent.getStringExtra("exerciseId").toString()
        exerciseImageUrl = intent.getStringExtra("exerciseImageUrl").toString()
        setupContentView()

        val exerciseName = intent.getStringExtra("exerciseName")
        val exerciseObservation = intent.getStringExtra("exerciseObservation")
        val exerciseTime = intent.getStringExtra("exerciseTime")
        val exerciseRepetition = intent.getStringExtra("exerciseRepetition")
        binding.editTextExerciseName.setText(exerciseName)
        binding.editTextCreateExerciseObservation.setText(exerciseObservation)
        binding.editTextCreateExerciseTime.setText(exerciseTime)
        binding.editTextCreateExerciseRepetition.setText(exerciseRepetition)
    }

    private fun setupContentView() {
        setupBackButton()
        setupSelectImageButton()
        if (intent.getStringExtra("exerciseId") != null) {
            setupUpdateExerciseButton()
            setupDeleteButton()
        } else {
            binding.delete.visibility = View.GONE
            setupCreateExerciseButton()
        }

        if (!exerciseImageUrl.isNullOrBlank()) {
            Glide.with(binding.root)
                .load(exerciseImageUrl)
                .into(binding.imageSelected)
        } else {
            binding.imageSelected.setImageResource(R.drawable.gym_klaus)
        }
    }

    private fun setupBackButton() {
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupCreateExerciseButton() {
        binding.saveExerciseButton.setOnClickListener {
            val exerciseName = binding.editTextExerciseName.text.toString()
            val exerciseObservation = binding.editTextCreateExerciseObservation.text.toString()
            val exerciseTime = binding.editTextCreateExerciseTime.text.toString()
            val exerciseRepetition = binding.editTextCreateExerciseRepetition.text.toString()

            val currentUser = auth.currentUser
            currentUser?.uid?.let { userId ->
                val exerciseMap = hashMapOf(
                    "exerciseName" to exerciseName,
                    "exerciseObservation" to exerciseObservation,
                    "exerciseTime" to exerciseTime,
                    "exerciseRepetition" to exerciseRepetition,
                    "exerciseImageUrl" to exerciseImageUrl.orEmpty()
                )

                createExercise(userId, exerciseMap)
            } ?: run {
                Log.e("CreateExerciseActivity", "User is null")
            }
            binding.saveExerciseButton.isEnabled = false
        }
    }

    private fun createExercise(userId: String, exerciseMap: Map<String, Any>) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .document(trainingId)
            .collection("exercises")
            .add(exerciseMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newExerciseId = task.result?.id
                    Log.d("db", "Success creating exercise! Document ID: $newExerciseId")

                    if (newExerciseId != null) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("db", "Error: Document ID is null")
                    }
                } else {
                    Log.e("db", "Error creating exercise", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error creating exercise", exception)
            }
    }

    private fun setupSelectImageButton() {
        binding.selectImageContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                setupUploadImage(selectedImageUri)
            } else {
                Log.e("CreateExerciseActivity", "Selected image URI is null")
            }
        }
    }

    private fun setupUploadImage(selectedImageUri: Uri) {
        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener { taskSnapshot ->
                handleUploadSuccess(selectedImageUri)
            }
            .addOnFailureListener { exception ->
                handleUploadFailure(exception)
            }
    }

    private fun handleUploadSuccess(selectedImageUri: Uri) {
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            exerciseImageUrl = uri.toString()
            if (!exerciseImageUrl.isNullOrBlank()) {
                Log.d("FirebaseStorage", "Image URL: $exerciseImageUrl")
                Glide.with(binding.root)
                    .load(selectedImageUri)
                    .into(binding.imageSelected)
            } else {
                Log.e("FirebaseStorage", "Image URL is empty")
            }
        }
    }

    private fun handleUploadFailure(exception: Exception) {
        Log.e("FirebaseStorage", "Upload failed", exception)
    }

    private fun setupDeleteButton() {
        binding.delete.visibility = View.VISIBLE
        binding.delete.setOnClickListener {
            val exerciseId = intent.getStringExtra("exerciseId") ?: return@setOnClickListener
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmação")
            builder.setMessage("Deseja realmente deletar este exercício?")
            builder.setPositiveButton("Sim") { dialog, which ->
                val currentUser = auth.currentUser
                currentUser?.uid?.let { userId ->
                    deleteExercise(userId, exerciseId)
                } ?: run {
                    Log.e("CreateExerciseActivity", "User is null")
                }
            }
            builder.setNegativeButton("Não") { dialog, which -> }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteExercise(userId: String, exerciseId: String) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .document(trainingId)
            .collection("exercises")
            .document(exerciseId)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("db", "Success deleting exercise! Document ID: $exerciseId")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("db", "Error deleting exercise", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error deleting exercise", exception)
            }
    }

    private fun setupUpdateExerciseButton() {
        binding.saveExerciseButton.setOnClickListener {
            val exerciseName = binding.editTextExerciseName.text.toString()
            val exerciseObservation = binding.editTextCreateExerciseObservation.text.toString()
            val exerciseTime = binding.editTextCreateExerciseTime.text.toString()
            val exerciseRepetition = binding.editTextCreateExerciseRepetition.text.toString()

            val currentUser = auth.currentUser
            currentUser?.uid?.let { userId ->
                val exerciseMap = hashMapOf(
                    "exerciseName" to exerciseName,
                    "exerciseObservation" to exerciseObservation,
                    "exerciseTime" to exerciseTime,
                    "exerciseRepetition" to exerciseRepetition,
                    "exerciseImageUrl" to exerciseImageUrl.orEmpty()
                )
                updateExercise(userId, exerciseId, exerciseMap)
            } ?: run {
                Log.e("CreateExerciseActivity", "User is null")
            }
        }
    }

    private fun updateExercise(userId: String, exerciseId: String, exerciseMap: Map<String, Any>) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .document(trainingId)
            .collection("exercises")
            .document(exerciseId)
            .set(exerciseMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("db", "Success updating exercise! Document ID: $exerciseId")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("db", "Error updating exercise", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error updating exercise", exception)
            }
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}