// ProfileViewModel.kt
package com.example.eclinic_summer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _specialization = MutableStateFlow("")
    val specialization: StateFlow<String> = _specialization.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _dateOfBirth = MutableStateFlow("")
    val dateOfBirth: StateFlow<String> = _dateOfBirth.asStateFlow()

    private val _pesel = MutableStateFlow("")
    val pesel: StateFlow<String> = _pesel.asStateFlow()

    private val _licenseNumber = MutableStateFlow("")
    val licenseNumber: StateFlow<String> = _licenseNumber.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = userRepository.getUser(userId)
                _user.value?.let {
                    _fullName.value = it.fullName
                    _email.value = it.email
                    _specialization.value = it.specialization ?: ""
                    _phone.value = it.phone ?: ""
                    _address.value = it.address ?: ""
                    _dateOfBirth.value = it.dateOfBirth ?: ""
                    _pesel.value = it.pesel ?: ""
                    _licenseNumber.value = it.licenseNumber ?: ""
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onSpecializationChange(value: String) { _specialization.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onAddressChange(value: String) { _address.value = value }
    fun onDateOfBirthChange(value: String) { _dateOfBirth.value = value }
    fun onPeselChange(value: String) { _pesel.value = value }
    fun onLicenseNumberChange(value: String) { _licenseNumber.value = value }

    fun updateUser() {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedUser = currentUser.copy(
                    fullName = fullName.value,
                    email = email.value,
                    phone = phone.value.ifEmpty { null },
                    address = address.value.ifEmpty { null },
                    dateOfBirth = dateOfBirth.value.ifEmpty { null },
                    pesel = pesel.value.ifEmpty { null },
                    specialization = specialization.value.ifEmpty { null },
                    licenseNumber = licenseNumber.value.ifEmpty { null },
                    updatedAt = Timestamp.now()
                )
                userRepository.updateUser(updatedUser)
                _isSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
}
