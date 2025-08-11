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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Automatycznie pobierz dane użytkownika przy starcie ViewModel
        fetchUserData()
    }

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
     * Logowanie użytkownika i pobranie danych z obsługą roli
     * onSuccess - callback, np. do nawigacji zależnej od roli użytkownika
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

                Log.d("AuthViewModel", "Login successful, fetching user data")
                fetchUserData(
                    onSuccess = {
                        Log.d("AuthViewModel", "User data fetched. Role: ${_currentUser.value?.role}")
                        val role = _currentUser.value?.role
                        onSuccess(role)
                    },
                    onError = {
                        Log.e("AuthViewModel", "Failed to fetch user data")
                        onSuccess(null)
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed: ${e.message}", e)
                _errorMessage.value = "Login failed: ${e.message}"
                onSuccess(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    /**
     * Pobranie danych użytkownika
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

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }
}
