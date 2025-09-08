package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for admin to manage all appointments.
 * Handles loading appointments and cancelling them.
 */
@HiltViewModel
class AdminAppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /** Loads all appointments from repository */
    fun loadAllAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _appointments.value = appointmentRepository.getAllAppointments()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Cancels an appointment and refreshes the list */
    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                appointmentRepository.updateAppointmentStatus(appointmentId, "cancelled")
                loadAllAppointments()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }
}
