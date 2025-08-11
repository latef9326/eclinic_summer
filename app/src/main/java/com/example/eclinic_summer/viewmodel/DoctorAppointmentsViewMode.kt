package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorAppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Dodaj StateFlow dla błędów
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var doctorId: String? = null

    init {
        doctorId = savedStateHandle["doctorId"]
        loadAppointments()
    }

    // Funkcja do ponownego ładowania danych
    fun retryLoadAppointments() {
        loadAppointments()
    }

    private fun loadAppointments() {
        val doctorId = this.doctorId ?: run {
            _isLoading.value = false
            _error.value = "Doctor ID is missing"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val appointmentsList = appointmentRepository
                    .getAppointmentsForDoctor(doctorId)
                    .first()

                _appointments.value = appointmentsList
            } catch (e: Exception) {
                _error.value = "Failed to load appointments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
