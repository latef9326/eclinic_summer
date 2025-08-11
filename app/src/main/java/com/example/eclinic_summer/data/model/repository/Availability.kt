package com.example.eclinic_summer.data.model.repository



// data/model/repository/Availability.kt
data class Availability(
    val date: String = "",        // Format: "2025-07-30"
    val dayOfWeek: String = "",   // Dodaj to pole z powrotem np. "Monday"
    val startTime: String = "",   // Format: "10:00"
    val endTime: String = "",     // Format: "11:00"
    val isBooked: Boolean = false
)
