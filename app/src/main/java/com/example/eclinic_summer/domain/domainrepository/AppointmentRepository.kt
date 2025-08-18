// AppointmentRepository.kt
package com.example.eclinic_summer.domain.domainrepository

import com.example.eclinic_summer.data.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    // --- Pacjent / Doktor ---
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>>
    fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>>
    suspend fun bookAppointment(appointment: Appointment): Result<Unit>

    // --- Historia medyczna ---
    suspend fun updateAppointmentNotes(
        appointmentId: String,
        notes: String
    ): Result<Unit>

    suspend fun addDocumentToAppointment(
        appointmentId: String,
        documentType: String, // "prescription" or "test_results"
        documentUrl: String
    ): Result<Unit>

    // --- Admin ---
    suspend fun getAllAppointments(): List<Appointment>
    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: String
    ): Result<Unit>

    suspend fun getAppointmentsByDoctor(doctorId: String): List<Appointment>
    suspend fun getAppointmentsByPatient(patientId: String): List<Appointment>
    // Add to interface
    suspend fun getAppointmentById(appointmentId: String): Appointment?
}