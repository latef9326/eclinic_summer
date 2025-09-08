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

/**
 * ViewModel for admin panel.
 * Manages doctors and patients: loading lists, deleting users.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _doctors = MutableStateFlow<List<User>>(emptyList())
    val doctors: StateFlow<List<User>> = _doctors.asStateFlow()

    private val _patients = MutableStateFlow<List<User>>(emptyList())
    val patients: StateFlow<List<User>> = _patients.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    /** Loads doctors and patients from repository */
    private fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _doctors.value = userRepository.getUsersByRole("doctor")
                _patients.value = userRepository.getUsersByRole("patient")
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Deletes a user by UID and refreshes the lists */
    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(uid)
                loadUsers()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }
}
