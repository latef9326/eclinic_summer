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
import javax.inject.Inject

@HiltViewModel
class PatientAppointmentViewModel @Inject constructor(
    private val getAvailability: GetDoctorAvailabilityUseCase,
    private val bookAppointment: BookAppointmentUseCase,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedDoctor = MutableStateFlow<String?>(null)
    fun selectDoctor(id: String) { _selectedDoctor.value = id }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val availability: StateFlow<List<Availability>> = _selectedDoctor
        .filterNotNull()
        .flatMapLatest { doctorId ->
            flow {
                try {
                    emit(getAvailability(doctorId))
                } catch (e: Exception) {
                    _error.value = e
                    emit(emptyList())
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    // ✅ zmienione na Result<Unit>
    private val _bookingStatus = MutableStateFlow<Result<Unit>?>(null)
    val bookingStatus: StateFlow<Result<Unit>?> = _bookingStatus.asStateFlow()

    fun book(slot: Availability) {
        val doctorId = _selectedDoctor.value ?: return

        val patientId = authRepository.getCurrentUserId() ?: run {
            _authError.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(patientId)
                if (user == null) {
                    _authError.value = "User data not found"
                    return@launch
                }

                val appointment = Appointment(
                    patientId = patientId,
                    doctorId = doctorId,
                    date = slot.date,
                    time = slot.startTime,
                    status = "scheduled"
                )

                val updatedSlot = slot.copy(isBooked = true)
                userRepository.updateAvailability(doctorId, slot, updatedSlot)

                // ✅ teraz bookAppointment zwraca Result<Unit>
                _bookingStatus.value = bookAppointment(appointment)
            } catch (e: Exception) {
                _bookingStatus.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetErrors() {
        _error.value = null
        _authError.value = null
        _bookingStatus.value = null
    }
}
