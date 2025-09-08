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

/**
 * ViewModel responsible for managing the user's profile data.
 * Handles loading, updating, and observing changes in user profile fields.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Current user object. */
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    /** User's full name. */
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    /** User's email address. */
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    /** User's specialization (if a doctor). */
    private val _specialization = MutableStateFlow("")
    val specialization: StateFlow<String> = _specialization.asStateFlow()

    /** User's phone number. */
    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    /** User's address. */
    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    /** User's date of birth. */
    private val _dateOfBirth = MutableStateFlow("")
    val dateOfBirth: StateFlow<String> = _dateOfBirth.asStateFlow()

    /** User's national identification number (PESEL). */
    private val _pesel = MutableStateFlow("")
    val pesel: StateFlow<String> = _pesel.asStateFlow()

    /** User's professional license number (if a doctor). */
    private val _licenseNumber = MutableStateFlow("")
    val licenseNumber: StateFlow<String> = _licenseNumber.asStateFlow()

    /** Loading state for profile operations. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Stores errors during profile operations. */
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    /** Flag indicating whether the last update operation was successful. */
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    /**
     * Loads the user's profile data from the repository.
     * Populates all individual field states with the fetched values.
     */
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

    /** Field change listeners to update state. */
    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onSpecializationChange(value: String) { _specialization.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onAddressChange(value: String) { _address.value = value }
    fun onDateOfBirthChange(value: String) { _dateOfBirth.value = value }
    fun onPeselChange(value: String) { _pesel.value = value }
    fun onLicenseNumberChange(value: String) { _licenseNumber.value = value }

    /**
     * Updates the user's profile in the repository with current field values.
     * Automatically sets the updatedAt timestamp.
     */
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
