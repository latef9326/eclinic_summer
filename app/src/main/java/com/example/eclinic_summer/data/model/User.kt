package com.example.eclinic_summer.data.model

import com.example.eclinic_summer.data.model.repository.Availability
import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "", // "patient", "doctor", "admin"
    val medicalHistory: String? = null,
    val specialization: String? = null, // tylko dla doctors
    val availability: List<Availability>? = null, // tylko dla doctors
    val createdAt: Timestamp = Timestamp.now()
)



