package com.example.desafiolealapps.data

import java.io.Serializable

data class ItemExercise(
    var exerciseId: String,
    var exerciseName: String,
    var exerciseObservation: String,
    var exerciseTime: String,
): Serializable
