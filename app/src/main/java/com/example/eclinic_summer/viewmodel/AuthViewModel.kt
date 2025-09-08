package com.example.eclinic_summer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for authentication, registration, and managing the current user.
 * Exposes loading states, error messages, and current user data.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** Indicates whether an operation is in progress. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Holds any error messages related to authentication. */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** The currently authenticated user. */
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Automatically fetch current user data when ViewModel starts
        fetchUserData()
    }

    /**
     * Registers a new user (patient or doctor) in Firebase Auth and Firestore.
     */
    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        role: String,
        specialization: String? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val authResult = authRepository.registerUser(email, password).getOrThrow()
                val uid = authResult.user?.uid ?: throw Exception("User UID is null")

                val user = User(
                    uid = uid,
                    email = email,
                    fullName = fullName,
                    role = role,
                    specialization = specialization
                )

                authRepository.saveUserToFirestore(user).getOrThrow()
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Logs in a user and fetches their data from Firestore.
     * @param onSuccess Callback invoked after successful login with the user's role.
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (role: String?) -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Attempting login for: $email")
                authRepository.loginUser(email, password).getOrThrow()

                fetchUserData(
                    onSuccess = {
                        val role = _currentUser.value?.role
                        onSuccess(role)
                    },
                    onError = { onSuccess(null) }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: ${e.message}"
                onSuccess(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Returns the UID of the currently logged-in user. */
    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    /**
     * Fetches the current user's data from Firestore.
     * @param onSuccess Callback invoked when data is successfully fetched.
     * @param onError Callback invoked in case of an error.
     */
    fun fetchUserData(
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        val uid = authRepository.getCurrentUserId() ?: run {
            onError()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUser(uid)
                _currentUser.value = user
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch user: ${e.message}"
                onError()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Logs out the currently authenticated user. */
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }
}
