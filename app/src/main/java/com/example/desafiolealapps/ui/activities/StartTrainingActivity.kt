package com.example.desafiolealapps.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiolealapps.data.ItemExercise
import com.example.desafiolealapps.databinding.ActivityStartTrainingBinding
import com.example.desafiolealapps.ui.adapters.AdapterStartExerciseList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StartTrainingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartTrainingBinding
    private lateinit var adapter: AdapterStartExerciseList
    private lateinit var auth: FirebaseAuth
    private lateinit var trainingId: String
    private val db = FirebaseFirestore.getInstance()
    private var serieCount: Int = 0
    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning = false
    private var initialTimeInMillis: Long = 0
    private var timeRemainingInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        trainingId = intent.getStringExtra("trainingId").toString()
        auth = FirebaseAuth.getInstance()
        setupContentView()
    }
    private fun setupContentView() {
        setupRecyclerViewStartTraining()
        setupBackButton()
        setupTimerButtons()
        setupTimerInitialButtons()
        setupSerieControl()
        initialTimeInMillis = 60 * 1000
        timeRemainingInMillis = initialTimeInMillis
        updateTimerUI()
    }

    private fun setupTimerButtons() {
        binding.startButton.setOnClickListener { toggleTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }
    }

    private fun setupTimerInitialButtons() {
        binding.increaseButton.setOnClickListener { increaseInitialTime() }
        binding.decreaseButton.setOnClickListener { decreaseInitialTime() }
    }

    private fun increaseInitialTime() {
        if (!isTimerRunning){
            initialTimeInMillis += 10 * 1000
            timeRemainingInMillis = initialTimeInMillis
            updateTimerUI()
        }
    }

    private fun decreaseInitialTime() {
        if (initialTimeInMillis > 10 * 1000 && !isTimerRunning) {
            initialTimeInMillis -= 10 * 1000
            timeRemainingInMillis = initialTimeInMillis
            updateTimerUI()
        } else {
            Toast.makeText(this, "Tempo mínimo atingido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                updateTimerUI()
            }

            override fun onFinish() {
                resetTimer()
                Toast.makeText(this@StartTrainingActivity, "Tempo de descanso encerrado", Toast.LENGTH_SHORT).show()
            }
        }

        countDownTimer.start()
        isTimerRunning = true
        binding.startButton.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        binding.startButton.text = "Resume"
    }

    private fun resetTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        timeRemainingInMillis = initialTimeInMillis
        updateTimerUI()
        binding.startButton.text = "Start"
    }

    private fun updateTimerUI() {
        val minutes = (timeRemainingInMillis / 1000) / 60
        val seconds = (timeRemainingInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.timerTextView.text = timeLeftFormatted
    }

    private fun setupSerieControl() {
        binding.decreaseSerieButton.setOnClickListener {
            decreaseSerieCount()
        }

        binding.increaseSerieButton.setOnClickListener {
            increaseSerieCount()
        }

        binding.resetSerieButton.setOnClickListener {
            resetSerieCount()
        }

        updateSerieUI()
    }

    private fun decreaseSerieCount() {
        if (serieCount > 0) {
            serieCount--
            updateSerieUI()
        }
    }

    private fun increaseSerieCount() {
        serieCount++
        updateSerieUI()
    }

    private fun resetSerieCount() {
        serieCount = 0
        updateSerieUI()
    }

    private fun updateSerieUI() {
        binding.serieTextView.text = serieCount.toString()
    }

    private fun setupBackButton(){
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerViewStartTraining() {
        adapter = AdapterStartExerciseList(this)
        binding.recyclerExercise.layoutManager = LinearLayoutManager(this)
        binding.recyclerExercise.adapter = adapter

        adapter.setOnItemLongClickListener(object : AdapterStartExerciseList.OnItemLongClickListener {
            override fun onItemLongClicked(exercise: ItemExercise): Boolean {
                val builder = AlertDialog.Builder(this@StartTrainingActivity)
                builder.setTitle("Observação")
                builder.setMessage(exercise.exerciseObservation)
                builder.setPositiveButton("OK") { _, _ -> }
                val dialog = builder.create()
                dialog.show()
                return true
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
                        val exerciseRepetition = document.getString("exerciseRepetition") ?: ""

                        val newItem = ItemExercise(
                            document.id,
                            exerciseName,
                            exerciseObservation,
                            exerciseTime,
                            exerciseRepetition,
                        )

                        exercisesList.add(newItem)
                    }
                    if(exercisesList.isEmpty()){
                        binding.noTrainingContainer.visibility = View.VISIBLE
                    }else{
                        binding.noTrainingContainer.visibility = View.INVISIBLE
                        adapter.setData(exercisesList)
                    }

                }
                .addOnFailureListener { exception ->
                    Log.e("EditTrainingActivity", "Error loading exercises", exception)
                }
        } ?: run {
            Log.e("EditTrainingActivity", "User is null")
        }
    }

}