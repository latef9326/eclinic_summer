package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing a patient's medical history.
 * Supports filtering, searching, sorting, and selecting appointments.
 */
@HiltViewModel
class MedicalHistoryViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** List of medical history items for the patient. */
    private val _appointments = MutableStateFlow<List<MedicalHistoryItem>>(emptyList())
    val appointments: StateFlow<List<MedicalHistoryItem>> = _appointments.asStateFlow()

    /** Indicates whether data is currently being loaded. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Stores error messages in case of failure. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Currently selected appointment. */
    private val _selectedAppointment = MutableStateFlow<MedicalHistoryItem?>(null)
    val selectedAppointment: StateFlow<MedicalHistoryItem?> = _selectedAppointment.asStateFlow()

    private val currentFilter = MutableStateFlow("all")
    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow("newest")

    /**
     * Loads the medical history for a specific patient, applying filter, search, and sort order.
     */
    fun loadMedicalHistory(patientId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                appointmentRepository.getAppointmentsForPatient(patientId)
                    .combine(currentFilter) { appointments, filter ->
                        appointments.filter {
                            when (filter) {
                                "completed" -> it.status == "completed"
                                "cancelled" -> it.status == "cancelled"
                                "e-consultation" -> it.consultationType == "e-consultation"
                                "in-person" -> it.consultationType == "in-person"
                                else -> true // all
                            }
                        }
                    }
                    .combine(sortOrder) { filteredAppointments, order ->
                        when (order) {
                            "oldest" -> filteredAppointments.sortedBy { "${it.date} ${it.time}" }
                            else -> filteredAppointments.sortedByDescending { "${it.date} ${it.time}" }
                        }
                    }
                    .collect { filteredList ->
                        val enrichedAppointments = mutableListOf<MedicalHistoryItem>()
                        for (appointment in filteredList) {
                            val doctor = userRepository.getUser(appointment.doctorId)
                            val item = MedicalHistoryItem(
                                id = appointment.appointmentId,
                                date = appointment.date,
                                time = appointment.time,
                                doctorName = doctor?.fullName ?: "Unknown Doctor",
                                doctorSpecialization = doctor?.specialization ?: "General",
                                status = appointment.status,
                                consultationType = appointment.consultationType,
                                doctorNotes = appointment.doctorNotes,
                                prescriptionUrl = appointment.prescriptionUrl,
                                testResultsUrl = appointment.testResultsUrl
                            )

                            if (
                                searchQuery.value.isBlank() ||
                                item.doctorName.contains(searchQuery.value, ignoreCase = true) ||
                                (item.doctorNotes?.contains(searchQuery.value, ignoreCase = true) == true)
                            ) {
                                enrichedAppointments.add(item)
                            }
                        }
                        _appointments.value = enrichedAppointments
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load medical history: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /** Selects an appointment for detailed view. */
    fun selectAppointment(item: MedicalHistoryItem) {
        _selectedAppointment.value = item
    }

    /** Clears the currently selected appointment. */
    fun clearSelection() {
        _selectedAppointment.value = null
    }

    /** Sets the filter for appointment status or type. */
    fun setFilter(filter: String) {
        currentFilter.value = filter
    }

    /** Sets the search query for doctor name or notes. */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /** Sets the sort order: "newest" or "oldest". */
    fun setSortOrder(order: String) {
        sortOrder.value = order
    }
}

/** Represents a single medical history item (appointment). */
data class MedicalHistoryItem(
    val id: String,
    val date: String,
    val time: String,
    val doctorName: String,
    val doctorSpecialization: String,
    val status: String,
    val consultationType: String,
    val doctorNotes: String?,
    val prescriptionUrl: String?,
    val testResultsUrl: String?
)
