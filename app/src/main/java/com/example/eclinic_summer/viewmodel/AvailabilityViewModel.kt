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

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

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

    fun deleteSlot(doctorId: String, slot: Availability) {
        viewModelScope.launch {
            try {
                userRepository.removeAvailability(doctorId, slot)
                loadAvailability(doctorId) // Refresh the list
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }
}