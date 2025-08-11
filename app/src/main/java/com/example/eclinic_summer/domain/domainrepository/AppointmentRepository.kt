package com.example.eclinic_summer.domain.domainrepository

import com.example.eclinic_summer.data.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>>
    fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>>
    suspend fun bookAppointment(appointment: Appointment): Result<Void?>

    // Add for medical history
    suspend fun updateAppointmentNotes(
        appointmentId: String,
        notes: String
    ): Result<Void?>

    suspend fun addDocumentToAppointment(
        appointmentId: String,
        documentType: String, // "prescription" or "test_results"
        documentUrl: String
    ): Result<Void?>
}