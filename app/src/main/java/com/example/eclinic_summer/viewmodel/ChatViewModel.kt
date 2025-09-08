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

/**
 * ViewModel responsible for managing chat functionality in the app.
 * Handles conversations, messages, sending text and file messages,
 * downloading files, and marking messages as read.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** List of all conversations for the current user. */
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    /** List of messages in the currently selected conversation. */
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    /** The currently selected conversation. */
    private var currentConversation: Conversation? = null

    /** Returns the current user's ID. */
    fun getCurrentUserId(): String = authRepository.getCurrentUserId() ?: ""

    /**
     * Loads all conversations for a given user.
     * @param userId The user ID for whom to fetch conversations.
     */
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

    /**
     * Loads messages for the selected conversation.
     * @param conversationId The conversation ID.
     */
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

    /**
     * Sends a text message in a given conversation.
     * @param conversationId The conversation ID.
     * @param text The message text.
     */
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

    /**
     * Sends a file message in a given conversation.
     * @param conversationId The conversation ID.
     * @param fileUri The file URI.
     * @param fileName The file name.
     */
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

    /**
     * Downloads a file from a given URL.
     * @param fileUrl The file URL.
     */
    fun downloadFile(fileUrl: String) {
        viewModelScope.launch {
            try {
                chatRepository.downloadFile(context, fileUrl)
            } catch (e: Exception) {
                Timber.e(e, "Error downloading file")
            }
        }
    }

    /**
     * Marks all messages in a conversation as read.
     * @param conversationId The conversation ID.
     */
    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(conversationId, getCurrentUserId())
        }
    }

    /** Returns the receiver ID for the current conversation. */
    private fun getReceiverId(conversationId: String): String {
        val currentUserId = getCurrentUserId()
        val conversation = currentConversation ?: return ""
        return conversation.participants.firstOrNull { it != currentUserId } ?: ""
    }

    /** Determines the file type based on the file extension. */
    private fun getFileType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif" -> "image"
            "pdf" -> "pdf"
            else -> "file"
        }
    }
}
