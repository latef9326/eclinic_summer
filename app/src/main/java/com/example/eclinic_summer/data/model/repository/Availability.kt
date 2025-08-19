package com.example.eclinic_summer.data.model.repository

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class Availability(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",

    @get:PropertyName("isBooked")
    @set:PropertyName("isBooked")
    var isBooked: Boolean = false
)