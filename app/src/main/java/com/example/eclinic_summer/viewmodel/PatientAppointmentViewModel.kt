package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.example.eclinic_summer.domain.domainrepository.usecase.BookAppointmentUseCase
import com.example.eclinic_summer.domain.domainrepository.usecase.GetDoctorAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PatientAppointmentViewModel @Inject constructor(
    private val getAvailability: GetDoctorAvailabilityUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedDoctor = MutableStateFlow<String?>(null)
    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _bookingStatus = MutableStateFlow<Result<Unit>?>(null)
    val bookingStatus: StateFlow<Result<Unit>?> = _bookingStatus.asStateFlow()

    fun selectDoctor(id: String) {
        _selectedDoctor.value = id
        loadAvailability()
    }

    private fun loadAvailability() {
        viewModelScope.launch {
            _selectedDoctor.value?.let { doctorId ->
                _isLoading.value = true
                try {
                    _availability.value = getAvailability(doctorId)
                    _error.value = null
                } catch (e: Exception) {
                    _error.value = e
                    _availability.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun book(slot: Availability) {
        val doctorId = _selectedDoctor.value ?: return
        val patientId = authRepository.getCurrentUserId() ?: run {
            _authError.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Natychmiastowa aktualizacja UI
                _availability.value = _availability.value.map {
                    if (it.id == slot.id) it.copy(isBooked = true) else it
                }

                // 2. Stwórz appointment
                val appointment = Appointment(
                    patientId = patientId,
                    doctorId = doctorId,
                    date = slot.date,
                    time = slot.startTime,
                    status = "scheduled"
                )

                // 3. Zaktualizuj availability doktora – używamy slotId + updatedSlot
                val updatedSlot = slot.copy(isBooked = true)
                val updateSuccess = userRepository.updateAvailability(
                    doctorId,
                    slot.id,
                    updatedSlot
                )

                if (!updateSuccess) {
                    throw Exception("Failed to update doctor availability")
                }

                // 4. Zapisz appointment
                _bookingStatus.value = bookAppointment(appointment)

            } catch (e: Exception) {
                // Cofnij zmianę w UI jeśli wystąpił błąd
                _availability.value = _availability.value.map {
                    if (it.id == slot.id) it.copy(isBooked = false) else it
                }
                _bookingStatus.value = Result.failure(e)
                Timber.e(e, "Booking failed for slot: ${slot.id}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isSlotBooked(slotId: String): Boolean {
        return _availability.value.any { it.id == slotId && it.isBooked }
    }

    fun resetErrors() {
        _error.value = null
        _authError.value = null
        _bookingStatus.value = null
    }
}
