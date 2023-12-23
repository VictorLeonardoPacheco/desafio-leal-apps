package com.example.desafiolealapps.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiolealapps.data.ItemExercise
import com.example.desafiolealapps.databinding.ActivityEditTrainingBinding
import com.example.desafiolealapps.ui.adapters.AdapterExerciseList
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class EditTrainingActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditTrainingBinding
    lateinit var trainingId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AdapterExerciseList
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val EXERCISE_REQUEST_CODE = 321
    }

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
        loadExercisesFromDatabase()
        setupGoToCreateExerciseButton()
        setupRecyclerViewExercises()

        if (intent.getStringExtra("trainingId") != null) {
            setupUpdateTrainingButton()
            setupDeleteButton()
        } else {
            binding.delete.visibility = View.GONE
            setupCreateTrainingButton()
        }
    }

    private fun setupRecyclerViewExercises() {
        adapter = AdapterExerciseList()
        binding.recyclerExercise.layoutManager = LinearLayoutManager(this)
        binding.recyclerExercise.adapter = adapter

        adapter.setOnItemClickListener(object : AdapterExerciseList.OnItemClickListener {
            override fun onItemClicked(exercise: ItemExercise) {
                editExercise(exercise, trainingId)
            }
        })

        loadExercisesFromDatabase()
    }

    private fun loadExercisesFromDatabase() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            db.collection("Users").document(userId)
                .collection("trainings")
                .document(trainingId)
                .collection("exercises")
                .get()
                .addOnSuccessListener { result ->
                    val exercisesList = mutableListOf<ItemExercise>()

                    for (document in result) {
                        val exerciseName = document.getString("exerciseName") ?: ""
                        val exerciseTime = document.getString("exerciseTime") ?: ""
                        val exerciseObservation = document.getString("exerciseObservation") ?: ""

                        val newItem = ItemExercise(
                            document.id,
                            exerciseName,
                            exerciseObservation,
                            exerciseTime
                        )

                        exercisesList.add(newItem)
                    }
                    adapter.setData(exercisesList)
                }
                .addOnFailureListener { exception ->
                    Log.e("EditTrainingActivity", "Error loading exercises", exception)
                }
        } ?: run {
            Log.e("EditTrainingActivity", "User is null")
        }
    }

    private fun setupGoToCreateExerciseButton() {
        binding.createExercise.setOnClickListener {
            val currentTrainingId = intent.getStringExtra("trainingId")

            if (currentTrainingId != null) {
                val intent = Intent(this, CreateExerciseActivity::class.java)
                intent.putExtra("trainingId", currentTrainingId)
                startActivityForResult(intent, EXERCISE_REQUEST_CODE)
            } else {
                createTrainingAndNavigateToCreateExercise()
            }
        }
    }

    private fun createTrainingAndNavigateToCreateExercise() {
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

            db.collection("Users").document(userId)
                .collection("trainings")
                .add(trainingMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newTrainingId = task.result?.id
                        Log.d("db", "Success creating training! Document ID: $newTrainingId")

                        if (newTrainingId != null) {
                            val intent = Intent(this, CreateExerciseActivity::class.java)
                            intent.putExtra("trainingId", newTrainingId)
                            startActivityForResult(intent, EXERCISE_REQUEST_CODE)
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
        } ?: run {
            Log.e("EditTrainingActivity", "User is null")
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


    private fun editExercise(exercise: ItemExercise, trainingId: String) {
        val intent = Intent(this, CreateExerciseActivity::class.java)
        intent.putExtra("exerciseId", exercise.exerciseId)
        intent.putExtra("trainingId", trainingId)
        intent.putExtra("exerciseName", exercise.exerciseName)
        intent.putExtra("exerciseTime", exercise.exerciseTime)
        intent.putExtra("exerciseObservation", exercise.exerciseObservation)
        startActivityForResult(intent, EXERCISE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EXERCISE_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("EditTraning", "Exercise created successfully")
            loadExercisesFromDatabase()
        }
    }

}
