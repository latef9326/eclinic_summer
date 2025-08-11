package com.example.eclinic_summer.data.model

data class Appointment(
    val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val date: String = "", // Format "yyyy-MM-dd"
    val time: String = "", // Format "HH:mm"
    val status: String = "scheduled", // "scheduled", "completed", "cancelled"
    val chatId: String = "",
    // Add new fields for medical history
    val doctorNotes: String? = null,
    val prescriptionUrl: String? = null,
    val testResultsUrl: String? = null,
    val consultationType: String = "e-consultation" // "e-consultation" or "in-person"
)