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
import javax.inject.Inject

/**
 * ViewModel responsible for managing a doctor's availability.
 * Provides functionality to load available time slots and remove specific slots.
 */
@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** List of available time slots for a doctor. */
    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    /** Indicates whether data is currently being loaded. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Holds any error that occurs during operations. */
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /**
     * Loads the availability of the specified doctor.
     * @param doctorId The UID of the doctor whose slots are being loaded.
     */
    fun loadAvailability(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(doctorId)
                _availability.value = user?.availability ?: emptyList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a specific availability slot for the doctor.
     * @param doctorId The UID of the doctor.
     * @param slot The availability slot to delete.
     */
    fun deleteSlot(doctorId: String, slot: Availability) {
        viewModelScope.launch {
            try {
                userRepository.removeAvailability(doctorId, slot)
                loadAvailability(doctorId) // Refresh the list after deletion
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }
}
