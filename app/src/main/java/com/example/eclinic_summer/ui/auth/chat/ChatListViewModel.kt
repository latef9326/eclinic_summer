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

/**
 * ViewModel responsible for managing the list of users available for chat.
 *
 * It handles loading users based on the current user's role (doctor or patient),
 * filtering users by search query, and creating new conversations.
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** StateFlow holding the list of loaded users. */
    private val _users = MutableStateFlow<List<User>>(emptyList())

    /** StateFlow holding the current search query for filtering users. */
    val searchQuery = MutableStateFlow("")

    /** StateFlow holding the current error message, if any. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** StateFlow with users filtered by the current search query. */
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

    /** Holds the role of the current user ("patient" or "doctor"). */
    private var currentUserRole: String? = null

    /**
     * Loads users depending on the role of the current user.
     * - Patient sees only doctors.
     * - Doctor sees only patients.
     *
     * @param currentUserId The ID of the currently logged-in user.
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
     * Creates a new conversation between a patient and a doctor.
     * - If the current user is a patient: (patientId = currentUserId, doctorId = otherUserId)
     * - If the current user is a doctor: (patientId = otherUserId, doctorId = currentUserId)
     *
     * @param currentUserId The ID of the currently logged-in user.
     * @param otherUserId The ID of the user to start a conversation with.
     * @param onSuccess Callback invoked with the new conversation ID upon successful creation.
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
