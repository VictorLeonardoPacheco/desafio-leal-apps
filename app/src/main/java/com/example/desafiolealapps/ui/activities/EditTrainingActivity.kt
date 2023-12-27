package com.example.desafiolealapps.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiolealapps.data.ItemExercise
import com.example.desafiolealapps.data.ItemTraining
import com.example.desafiolealapps.databinding.ActivityEditTrainingBinding
import com.example.desafiolealapps.ui.adapters.AdapterExerciseList
import com.example.desafiolealapps.ui.adapters.AdapterStartExerciseList
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
    private lateinit var trainingName: String
    private lateinit var trainingDescription: String
    private lateinit var trainingDays: String

    companion object {
        private const val EXERCISE_REQUEST_CODE = 321
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        trainingId = intent.getStringExtra("trainingId").toString()
        trainingName = intent.getStringExtra("trainingName").toString()
        trainingDescription = intent.getStringExtra("trainingDescription").toString()
        trainingDays = intent.getStringExtra("trainingDays").toString()
        binding.editTextTrainingName.setText(trainingName)
        setCheckedDays(trainingDays)
        binding.editTextTrainingDescription.setText(trainingDescription)
        setupContentView()

    }

    private fun setupContentView() {
        binding.createExercise.isEnabled = false
        binding.createExercise.alpha = 0.3f
        setupBackButton()
        loadExercisesFromDatabase()
        setupGoToCreateExerciseButton()
        setupGoToStartTrainingButton()
        setupRecyclerViewExercises()

        if (intent.getStringExtra("trainingId") != null) {
            enableCreateExerciseButton()
            setupDeleteButton()
        } else {
            binding.delete.visibility = View.GONE
        }
    }

    private fun setupBackButton() {
        binding.back.setOnClickListener {

            val currentTrainingDays = getSelectedDaysString()
            val currentTrainingDescription = binding.editTextTrainingDescription.text.toString()
            val currentTrainingName = binding.editTextTrainingName.text.toString()

            if (trainingDays != currentTrainingDays ||
                trainingDescription != currentTrainingDescription ||
                trainingName != currentTrainingName
            ) {
                showConfirmationDialog()
            } else {
                onBackPressed()
            }
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
        builder.setMessage("Deseja salvar as alterações?")

        builder.setPositiveButton("Sim") { dialog, which ->
            updateTrainingButton()
            setResult(RESULT_OK)
        }

        builder.setNegativeButton("Não") { dialog, which ->
            onBackPressed()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun getSelectedDaysString(): String {
        val selectedDays = mutableListOf<String>()

        if (binding.checkboxSunday.isChecked) {
            selectedDays.add("dom")
        }
        if (binding.checkboxMonday.isChecked) {
            selectedDays.add("seg")
        }
        if (binding.checkboxTuesday.isChecked) {
            selectedDays.add("ter")
        }
        if (binding.checkboxWednesday.isChecked) {
            selectedDays.add("qua")
        }
        if (binding.checkboxThursday.isChecked) {
            selectedDays.add("qui")
        }
        if (binding.checkboxFriday.isChecked) {
            selectedDays.add("sex")
        }
        if (binding.checkboxSaturday.isChecked) {
            selectedDays.add("sáb")
        }

        return selectedDays.joinToString(", ")
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

        adapter.setOnItemLongClickListener(object : AdapterExerciseList.OnItemLongClickListener {
            override fun onItemLongClicked(exercise: ItemExercise): Boolean {
                val builder = AlertDialog.Builder(this@EditTrainingActivity)
                builder.setTitle("Observação")
                builder.setMessage(exercise.exerciseObservation)
                builder.setPositiveButton("OK") { _, _ ->
                }

                val dialog = builder.create()
                dialog.show()
                return true
            }
        })

        loadExercisesFromDatabase()
    }

    private fun setCheckedDays(selectedDays: String?) {

        val daysArray = selectedDays?.split(", ")?.toTypedArray()

        if (daysArray != null) {
            for (day in daysArray) {
                when (day) {
                    "dom" -> binding.checkboxSunday.isChecked = true
                    "seg" -> binding.checkboxMonday.isChecked = true
                    "ter" -> binding.checkboxTuesday.isChecked = true
                    "qua" -> binding.checkboxWednesday.isChecked = true
                    "qui" -> binding.checkboxThursday.isChecked = true
                    "sex" -> binding.checkboxFriday.isChecked = true
                    "sáb" -> binding.checkboxSaturday.isChecked = true
                }
            }
        }
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
                        val exerciseRepetition = document.getString("exerciseRepetition") ?: ""
                        val exerciseImageUrl = document.getString("exerciseImageUrl") ?: ""

                        val newItem = ItemExercise(
                            document.id,
                            exerciseName,
                            exerciseObservation,
                            exerciseTime,
                            exerciseRepetition,
                            exerciseImageUrl,
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
        val currentTrainingId = trainingId
        val intent = Intent(this, CreateExerciseActivity::class.java)
        intent.putExtra("trainingId", currentTrainingId)
        binding.createExercise.setOnClickListener {
            startActivityForResult(intent, EXERCISE_REQUEST_CODE)
        }
    }

    private fun setupGoToStartTrainingButton() {
        val currentTrainingId = trainingId
        val intent = Intent(this, StartTrainingActivity::class.java)
        intent.putExtra("trainingId", currentTrainingId)
        binding.startTraining.setOnClickListener {
            startActivityForResult(intent, EXERCISE_REQUEST_CODE)
        }
    }

    private fun setupDeleteButton() {
        binding.delete.visibility = View.VISIBLE
        binding.delete.setOnClickListener {
            val trainingId = intent.getStringExtra("trainingId") ?: return@setOnClickListener

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmação")
            builder.setMessage("Deseja realmente deletar este treino?")
            builder.setPositiveButton("Sim") { dialog, which ->
                val currentUser = auth.currentUser

                currentUser?.uid?.let { userId ->
                    deleteTraining(userId, trainingId)
                } ?: run {
                    Log.e("EditTrainingActivity", "User is null")
                }
            }
            builder.setNegativeButton("Não") { dialog, which -> }
            val dialog = builder.create()
            dialog.show()
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

    private fun updateTrainingButton() {
            val trainingId = intent.getStringExtra("trainingId") ?: return
            val trainingName = binding.editTextTrainingName.text.toString()
            val trainingDescription = binding.editTextTrainingDescription.text.toString()
            val trainingDays = getSelectedDaysString()

            val currentUser = auth.currentUser
            currentUser?.uid?.let { userId ->
                val trainingMap = hashMapOf(
                    "trainingName" to trainingName,
                    "trainingDescription" to trainingDescription,
                    "trainingDays" to trainingDays
                )

                updateTraining(userId, trainingId, trainingMap)
            } ?: run {
                Log.e("EditTrainingActivity", "User is null")
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
                    onBackPressed()
                } else {
                    Log.e("db", "Error updating training", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("db", "Error updating training", exception)
            }
    }

    private fun enableCreateExerciseButton(){
        binding.createExercise.isEnabled = true
        binding.createExercise.alpha = 1f
        setupGoToCreateExerciseButton()
    }


    private fun editExercise(exercise: ItemExercise, trainingId: String) {
        val intent = Intent(this, CreateExerciseActivity::class.java)
        intent.putExtra("exerciseId", exercise.exerciseId)
        intent.putExtra("trainingId", trainingId)
        intent.putExtra("exerciseName", exercise.exerciseName)
        intent.putExtra("exerciseTime", exercise.exerciseTime)
        intent.putExtra("exerciseObservation", exercise.exerciseObservation)
        intent.putExtra("exerciseRepetition", exercise.exerciseRepetition)
        intent.putExtra("exerciseImageUrl", exercise.exerciseImageUrl)
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
