package com.example.desafiolealapps.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiolealapps.R
import com.example.desafiolealapps.data.ItemTraining
import com.example.desafiolealapps.databinding.ActivityMainBinding
import com.example.desafiolealapps.ui.adapters.AdapterTrainingList
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AdapterTrainingList
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TRAINING_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupContentView()
    }

    private fun setupContentView() {
        setupButtons()
        setupRecyclerViewTraining()
        loadTrainingsFromDatabase()
    }

    private fun setupButtons() {
        binding.myAccount.setOnClickListener {
            goToMyAccountActivity()
        }

        binding.floatingActionButton.setOnClickListener {
            exibirDialog(this)
        }
    }

    private fun exibirDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_create_training, null)

        builder.setView(dialogView)
            .setTitle("Criar Rotina de Treino")
            .setPositiveButton("Salvar") { dialog, which ->

                val trainingNameLayout = dialogView.findViewById<TextInputLayout>(R.id.edit_text_training_name_layout)
                val trainingDescriptionLayout = dialogView.findViewById<TextInputLayout>(R.id.edit_text_training_description_layout)

                val trainingName = trainingNameLayout.findViewById<EditText>(R.id.edit_text_training_name).text.toString()
                val trainingDescription = trainingDescriptionLayout.findViewById<EditText>(R.id.edit_text_training_description).text.toString()
                val trainingDays = getSelectedDaysString(dialogView)

                val currentUser = auth.currentUser
                currentUser?.uid?.let { userId ->
                    val trainingMap = hashMapOf(
                        "trainingName" to trainingName,
                        "trainingDescription" to trainingDescription,
                        "trainingDays" to trainingDays
                    )

                    createTraining(userId, trainingMap)
                }
            }
            .setNegativeButton("Cancelar") { dialog, which ->
            }

        val dialog = builder.create()
        dialog.show()
    }

    fun getSelectedDaysString(dialogView: View): String {
        val selectedDays = mutableListOf<String>()

        val checkboxSunday = dialogView.findViewById<CheckBox>(R.id.checkbox_sunday)
        val checkboxMonday = dialogView.findViewById<CheckBox>(R.id.checkbox_monday)
        val checkboxTuesday = dialogView.findViewById<CheckBox>(R.id.checkbox_tuesday)
        val checkboxWednesday = dialogView.findViewById<CheckBox>(R.id.checkbox_wednesday)
        val checkboxThursday = dialogView.findViewById<CheckBox>(R.id.checkbox_thursday)
        val checkboxFriday = dialogView.findViewById<CheckBox>(R.id.checkbox_friday)
        val checkboxSaturday = dialogView.findViewById<CheckBox>(R.id.checkbox_saturday)

        if (checkboxSunday.isChecked) {
            selectedDays.add("dom")
        }
        if (checkboxMonday.isChecked) {
            selectedDays.add("seg")
        }
        if (checkboxTuesday.isChecked) {
            selectedDays.add("ter")
        }
        if (checkboxWednesday.isChecked) {
            selectedDays.add("qua")
        }
        if (checkboxThursday.isChecked) {
            selectedDays.add("qui")
        }
        if (checkboxFriday.isChecked) {
            selectedDays.add("sex")
        }
        if (checkboxSaturday.isChecked) {
            selectedDays.add("sáb")
        }

        return selectedDays.joinToString(", ")
    }

    private fun createTraining(userId: String, trainingMap: Map<String, Any>) {
        db.collection("Users").document(userId)
            .collection("trainings")
            .add(trainingMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newTrainingId = task.result?.id
                    Log.d("db", "Success creating training! Document ID: $newTrainingId")
                    loadTrainingsFromDatabase()
                    if (newTrainingId != null) {
                        setResult(RESULT_OK)
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

    private fun goToMyAccountActivity() {
        val intent = Intent(this, MyAccountActivity::class.java)
        startActivity(intent)
    }

    private fun setupRecyclerViewTraining() {
        adapter = AdapterTrainingList()
        binding.recyclerTraining.layoutManager = LinearLayoutManager(this)
        binding.recyclerTraining.adapter = adapter

        adapter.setOnItemClickListener(object : AdapterTrainingList.OnItemClickListener {
            override fun onItemClicked(training: ItemTraining) {
                editTraining(training)
            }

            override fun onStartTrainingClicked(training: ItemTraining) {
                startTraining(training)
            }
        })
        loadTrainingsFromDatabase()
    }

    private fun loadTrainingsFromDatabase() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            db.collection("Users").document(userId)
                .collection("trainings")
                .get()
                .addOnSuccessListener { result ->
                    val trainingsList = mutableListOf<ItemTraining>()

                    for (document in result) {
                        val trainingName = document.getString("trainingName") ?: ""
                        val trainingDays = document.getString("trainingDays") ?: ""
                        val trainingDescription = document.getString("trainingDescription") ?: ""

                        val newItem = ItemTraining(
                            document.id,
                            trainingDescription,
                            trainingName,
                            trainingDays
                        )

                        trainingsList.add(newItem)
                    }
                    if (trainingsList.isEmpty()){
                        binding.noTrainingContainer.visibility = View.VISIBLE
                    } else {
                        binding.noTrainingContainer.visibility = View.GONE
                        adapter.run {
                            setData(trainingsList)
                            notifyDataSetChanged()
                        }
                    }
                }
        } ?: run {
            Log.e("MainActivity", "User is null")
        }
    }

    private fun editTraining(training: ItemTraining) {
        val intent = Intent(this, EditTrainingActivity::class.java)
        intent.putExtra("trainingId", training.trainingId)
        intent.putExtra("trainingName", training.name)
        intent.putExtra("trainingDays", training.trainingDays)
        intent.putExtra("trainingDescription", training.description)
        startActivityForResult(intent, TRAINING_REQUEST_CODE)
    }

    private fun startTraining(training: ItemTraining) {
        val intent = Intent(this, StartTrainingActivity::class.java)
        intent.putExtra("trainingId", training.trainingId)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TRAINING_REQUEST_CODE && resultCode == RESULT_OK) {
            loadTrainingsFromDatabase()
        }
    }
}