package com.example.eclinic_summer.data.model.repository

import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    private val appointmentsRef = firestore.collection("appointments")

    override fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>> {
        return appointmentsRef
            .whereEqualTo("doctorId", doctorId)
            .snapshots()
            .map { snap ->
                println("Firestore query returned ${snap.size()} documents for doctor $doctorId")
                snap.documents.mapNotNull { doc -> doc.toAppointment() }
            }
    }

    override fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>> {
        return appointmentsRef
            .whereEqualTo("patientId", patientId)
            .snapshots()
            .map { snap ->
                println("Firestore query returned ${snap.size()} documents for patient $patientId")
                snap.documents.mapNotNull { doc -> doc.toAppointment() }
            }
    }

    override suspend fun bookAppointment(appointment: Appointment): Result<Void?> {
        return try {
            val docId = if (appointment.appointmentId.isBlank()) {
                appointmentsRef.document().id
            } else {
                appointment.appointmentId
            }

            appointmentsRef.document(docId)
                .set(appointment.copy(appointmentId = docId))
                .await()

            Result.success(null)
        } catch (e: Exception) {
            println("Error booking appointment: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateAppointmentNotes(
        appointmentId: String,
        notes: String
    ): Result<Void?> {
        return try {
            appointmentsRef.document(appointmentId)
                .update("doctorNotes", notes)
                .await()
            Result.success(null)
        } catch (e: Exception) {
            println("Error updating notes: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun addDocumentToAppointment(
        appointmentId: String,
        documentType: String,
        documentUrl: String
    ): Result<Void?> {
        return try {
            val fieldName = when (documentType) {
                "prescription" -> "prescriptionUrl"
                "test_results" -> "testResultsUrl"
                else -> throw IllegalArgumentException("Unknown document type")
            }

            appointmentsRef.document(appointmentId)
                .update(fieldName, documentUrl)
                .await()
            Result.success(null)
        } catch (e: Exception) {
            println("Error adding document: ${e.message}")
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toAppointment(): Appointment? {
        return try {
            this.toObject(Appointment::class.java)?.copy(appointmentId = this.id)
        } catch (e: Exception) {
            println("Error converting document ${this.id}: ${e.message}")
            null
        }
    }
}