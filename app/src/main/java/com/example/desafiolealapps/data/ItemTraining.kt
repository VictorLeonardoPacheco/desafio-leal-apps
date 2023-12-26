package com.example.desafiolealapps.data

import java.io.Serializable

data class ItemTraining(
    var trainingId: String,
    var description: String,
    var name: String,
    var trainingDays: String,
): Serializable
