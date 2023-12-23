package com.example.desafiolealapps.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiolealapps.data.ItemTraining
import com.example.desafiolealapps.databinding.ActivityMainBinding
import com.example.desafiolealapps.ui.adapters.AdapterTrainingList
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
            goToEditTrainingActivity()
        }
    }

    private fun goToMyAccountActivity() {
        val intent = Intent(this, MyAccountActivity::class.java)
        startActivity(intent)
    }

    private fun goToEditTrainingActivity() {
        val intent = Intent(this, EditTrainingActivity::class.java)
        startActivityForResult(intent, TRAINING_REQUEST_CODE)
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

        if (adapter == null){
            binding.noTrainingContainer.visibility = View.VISIBLE
        } else {
            binding.noTrainingContainer.visibility = View.GONE
        }

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
                        val trainingTime = document.getString("trainingTime") ?: ""
                        val trainingDescription = document.getString("trainingDescription") ?: ""

                        val newItem = ItemTraining(
                            document.id,
                            trainingDescription,
                            trainingName,
                            trainingTime
                        )

                        trainingsList.add(newItem)
                    }


                    if (trainingsList.isEmpty()){
                        binding.noTrainingContainer.visibility = View.VISIBLE
                    } else {
                        binding.noTrainingContainer.visibility = View.GONE
                        adapter.setData(trainingsList)
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
        intent.putExtra("trainingTime", training.time)
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
            Log.d("MainActivity", "Training created successfully")
            loadTrainingsFromDatabase()
        }
    }
}