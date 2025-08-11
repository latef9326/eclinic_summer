package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _doctors = MutableStateFlow<List<User>>(emptyList())
    val doctors: StateFlow<List<User>> = _doctors.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        loadDoctors()
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            try {
                println("DEBUG: [DoctorListVM] Fetching doctors...")
                _isLoading.value = true

                val doctorsList = userRepository.getUsersByRole("doctor")
                println("DEBUG: [DoctorListVM] Found ${doctorsList.size} doctors")

                if (doctorsList.isEmpty()) {
                    println("WARN: [DoctorListVM] No doctors found in database")
                }

                _doctors.value = doctorsList
                _error.value = null
            } catch (e: Exception) {
                println("ERROR: [DoctorListVM] ${e.message}")
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
}
