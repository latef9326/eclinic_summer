package com.example.eclinic_summer.data.model

import com.example.eclinic_summer.data.model.repository.Availability
import com.google.firebase.Timestamp

/**
 * Represents a user of the system (doctor, patient, or admin).
 *
 * @property uid Unique identifier of the user.
 * @property email Email address of the user.
 * @property fullName Full name of the user.
 * @property role User role ("patient", "doctor", "admin").
 * @property phone Optional phone number.
 * @property address Optional home address.
 * @property dateOfBirth Optional date of birth (format: "yyyy-MM-dd").
 * @property pesel Optional PESEL number (Polish national identification).
 * @property medicalHistory Optional medical history for patients.
 * @property specialization Doctor’s specialization (only for doctors).
 * @property licenseNumber License number of the doctor (only for doctors).
 * @property availability List of availability slots (only for doctors).
 * @property fcmToken Firebase Cloud Messaging token for push notifications.
 * @property createdAt Timestamp of when the account was created.
 * @property updatedAt Timestamp of last account update.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "",
    val phone: String? = null,
    val address: String? = null,
    val dateOfBirth: String? = null,
    val pesel: String? = null,
    val medicalHistory: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val availability: List<Availability>? = null,
    val fcmToken: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
