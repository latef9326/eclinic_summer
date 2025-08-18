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

@HiltViewModel
class AppointmentDetailViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {
    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Create a method in repository to get single appointment
                _appointment.value = appointmentRepository.getAppointmentById(appointmentId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
}