package com.example.eclinic_summer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.Conversation
import com.example.eclinic_summer.data.model.Message
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.example.eclinic_summer.domain.domainrepository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var currentConversation: Conversation? = null

    // Pobranie ID aktualnego użytkownika
    fun getCurrentUserId(): String = authRepository.getCurrentUserId() ?: ""

    fun loadConversations(userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.getConversations(userId).collect {
                    _conversations.value = it
                    Timber.d("Loaded ${it.size} conversations for user $userId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading conversations for user $userId")
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            currentConversation = _conversations.value.find { it.conversationId == conversationId }

            try {
                chatRepository.getMessages(conversationId).collect {
                    _messages.value = it
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading messages for conversation $conversationId")
            }
        }
    }

    fun sendTextMessage(conversationId: String, text: String) {
        viewModelScope.launch {
            val receiverId = getReceiverId(conversationId)
            val message = Message(
                conversationId = conversationId,
                senderId = getCurrentUserId(),
                receiverId = receiverId,
                text = text,
                type = "text"
            )
            chatRepository.sendMessage(message)
        }
    }

    fun sendFileMessage(conversationId: String, fileUri: Uri, fileName: String) {
        viewModelScope.launch {
            try {
                val receiverId = getReceiverId(conversationId)
                val fileUrl = chatRepository.uploadFile(fileUri, fileName)

                val message = Message(
                    conversationId = conversationId,
                    senderId = getCurrentUserId(),
                    receiverId = receiverId,
                    fileUrl = fileUrl,
                    fileName = fileName,
                    type = getFileType(fileName)
                )
                chatRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Error sending file message")
            }
        }
    }

    fun downloadFile(fileUrl: String) {
        viewModelScope.launch {
            try {
                chatRepository.downloadFile(context, fileUrl)
            } catch (e: Exception) {
                Timber.e(e, "Error downloading file")
            }
        }
    }

    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(conversationId, getCurrentUserId())
        }
    }

    private fun getReceiverId(conversationId: String): String {
        val currentUserId = getCurrentUserId()
        val conversation = currentConversation ?: return ""
        return conversation.participants.firstOrNull { it != currentUserId } ?: ""
    }

    private fun getFileType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif" -> "image"
            "pdf" -> "pdf"
            else -> "file"
        }
    }
}