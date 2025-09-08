package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel responsible for managing a doctor's schedule.
 * Supports loading availability, adding, updating, and deleting time slots.
 */
@HiltViewModel
class DoctorScheduleViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** List of availability slots for the doctor. */
    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    /** Indicates whether an operation is currently loading. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Holds any error that occurs during operations. */
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /**
     * Loads the availability slots for a given doctor.
     */
    fun loadAvailability(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(doctorId)
                _availability.value = user?.availability ?: emptyList()
                _error.value = null
            } catch (t: Throwable) {
                _availability.value = emptyList()
                _error.value = t
                Timber.e(t, "Failed to load availability for doctor: $doctorId")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Adds a new availability slot for a doctor.
     */
    fun addNewSlot(doctorId: String, newSlot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRepository.updateUserAvailability(doctorId, newSlot)) {
                    _availability.value = _availability.value + newSlot
                }
            } catch (t: Throwable) {
                _error.value = t
                Timber.e(t, "Failed to add new slot for doctor: $doctorId")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes an existing availability slot for a doctor.
     */
    fun deleteSlot(doctorId: String, slot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRepository.removeAvailability(doctorId, slot)) {
                    _availability.value = _availability.value.filter { it.id != slot.id }
                }
            } catch (t: Throwable) {
                _error.value = t
                Timber.e(t, "Failed to delete slot ${slot.id} for doctor: $doctorId")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates an existing availability slot for a doctor.
     */
    fun updateSlot(doctorId: String, oldSlot: Availability, newSlot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val slotId = oldSlot.id
                require(slotId.isNotBlank()) { "Slot ID cannot be blank" }

                if (userRepository.updateAvailability(doctorId, slotId, newSlot)) {
                    _availability.value = _availability.value.map { current ->
                        if (current.id == slotId) newSlot else current
                    }
                }
            } catch (t: Throwable) {
                _error.value = t
                Timber.e(t, "Failed to update slot ${oldSlot.id} for doctor: $doctorId")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
