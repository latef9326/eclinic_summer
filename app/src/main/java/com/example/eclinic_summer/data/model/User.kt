package com.example.eclinic_summer.data.model

import com.example.eclinic_summer.data.model.repository.Availability
import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "", // "patient", "doctor", "admin"
    val phone: String? = null, // numer telefonu
    val address: String? = null, // adres
    val dateOfBirth: String? = null, // data urodzenia w formacie string (np. "1990-01-15")
    val pesel: String? = null, // PESEL (dla polskiego systemu)
    val medicalHistory: String? = null,
    val specialization: String? = null, // tylko dla doctors
    val licenseNumber: String? = null, // numer licencji lekarza
    val availability: List<Availability>? = null, // tylko dla doctors
    val fcmToken: String? = null, // token FCM dla powiadomień
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now() // data ostatniej aktualizacji
)