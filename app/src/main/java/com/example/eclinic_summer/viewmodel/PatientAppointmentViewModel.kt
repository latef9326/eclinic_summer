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

/**
 * ViewModel responsible for managing patient appointments.
 * Supports selecting a doctor, loading availability, booking appointments, and error handling.
 */
@HiltViewModel
class PatientAppointmentViewModel @Inject constructor(
    private val getAvailability: GetDoctorAvailabilityUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Currently selected doctor ID. */
    private val _selectedDoctor = MutableStateFlow<String?>(null)

    /** Doctor's available slots. */
    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    /** Indicates whether a loading operation is in progress. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Stores general errors during data fetching. */
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /** Stores authentication-related errors. */
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    /** Status of the booking operation. */
    private val _bookingStatus = MutableStateFlow<Result<Unit>?>(null)
    val bookingStatus: StateFlow<Result<Unit>?> = _bookingStatus.asStateFlow()

    /** Selects a doctor and loads their availability. */
    fun selectDoctor(id: String) {
        _selectedDoctor.value = id
        loadAvailability()
    }

    /** Loads the availability slots for the selected doctor. */
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

    /**
     * Books an appointment for the selected slot.
     * Updates UI immediately and reverts if an error occurs.
     */
    fun book(slot: Availability) {
        val doctorId = _selectedDoctor.value ?: return
        val patientId = authRepository.getCurrentUserId() ?: run {
            _authError.value = "User not authenticated"
            return
        }

        // Check if the slot is still available
        if (slot.getStatus() != "available") {
            _bookingStatus.value = Result.failure(Exception("This time slot is no longer available"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Immediate UI update
                _availability.value = _availability.value.map {
                    if (it.id == slot.id) it.copy(isBooked = true) else it
                }

                // 2. Create appointment object
                val appointment = Appointment(
                    patientId = patientId,
                    doctorId = doctorId,
                    date = slot.date,
                    time = slot.startTime,
                    status = "scheduled"
                )

                // 3. Update doctor availability
                val updatedSlot = slot.copy(isBooked = true)
                val updateSuccess = userRepository.updateAvailability(doctorId, slot.id, updatedSlot)

                if (!updateSuccess) throw Exception("Failed to update doctor availability")

                // 4. Save appointment
                _bookingStatus.value = bookAppointment(appointment)

            } catch (e: Exception) {
                // Revert UI change if booking failed
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

    /** Resets all error states and booking status. */
    fun resetErrors() {
        _error.value = null
        _authError.value = null
        _bookingStatus.value = null
    }
}
