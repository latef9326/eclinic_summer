package com.example.eclinic_summer.ui.auth.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.example.eclinic_summer.domain.domainrepository.ChatRepository
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val filteredUsers = combine(_users, searchQuery) { users, query ->
        if (query.isEmpty()) users
        else users.filter {
            it.fullName.contains(query, ignoreCase = true)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var currentUserRole: String? = null

    /**
     * Ładuje użytkowników w zależności od roli zalogowanego użytkownika.
     * - Pacjent widzi tylko lekarzy.
     * - Lekarz widzi tylko pacjentów.
     */
    fun loadUsers(currentUserId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUserId)
                currentUserRole = user?.role

                when (currentUserRole) {
                    "patient" -> {
                        _users.value = userRepository.getUsersByRole("doctor")
                        Timber.d("Loaded ${_users.value.size} doctors")
                    }
                    "doctor" -> {
                        _users.value = userRepository.getUsersByRole("patient")
                        Timber.d("Loaded ${_users.value.size} patients")
                    }
                    else -> {
                        _users.value = emptyList()
                        Timber.w("Unknown role: $currentUserRole")
                    }
                }
                _error.value = null
            } catch (e: Exception) {
                Timber.e(e, "Error loading users")
                _error.value = "Failed to load users. Please try again."
            }
        }
    }

    /**
     * Tworzy nową konwersację między pacjentem a lekarzem.
     * - Jeśli użytkownik to pacjent: (patientId = currentUserId, doctorId = otherUserId)
     * - Jeśli użytkownik to lekarz: (patientId = otherUserId, doctorId = currentUserId)
     */
    fun createConversation(currentUserId: String, otherUserId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val conversationId = if (currentUserRole == "patient") {
                    chatRepository.createConversation(currentUserId, otherUserId)
                } else {
                    chatRepository.createConversation(otherUserId, currentUserId)
                }
                onSuccess(conversationId)
                _error.value = null
            } catch (e: Exception) {
                Timber.e(e, "Error creating conversation")
                _error.value = "Failed to start conversation. Please try again."
            }
        }
    }
}
