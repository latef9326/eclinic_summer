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
class DoctorScheduleViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadAvailability(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.getUser(doctorId)?.availability?.let {
                    _availability.value = it
                } ?: run {
                    _availability.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNewSlot(doctorId: String, newSlot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRepository.updateUserAvailability(doctorId, newSlot)) {
                    _availability.value = _availability.value + newSlot
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSlot(doctorId: String, slot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRepository.removeAvailability(doctorId, slot)) {
                    _availability.value = _availability.value.filter { it != slot }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSlot(doctorId: String, oldSlot: Availability, newSlot: Availability) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (userRepository.updateAvailability(doctorId, oldSlot, newSlot)) {
                    _availability.value = _availability.value.map {
                        if (it == oldSlot) newSlot else it
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
