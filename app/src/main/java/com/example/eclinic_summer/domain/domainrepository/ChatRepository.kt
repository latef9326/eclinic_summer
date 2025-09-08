package com.example.eclinic_summer.domain.domainrepository

import android.content.Context
import android.net.Uri
import com.example.eclinic_summer.data.model.Message
import com.example.eclinic_summer.data.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling chat conversations and messages.
 */
interface ChatRepository {
    suspend fun sendMessage(message: Message)
    fun getMessages(conversationId: String): Flow<List<Message>>
    fun getConversations(userId: String): Flow<List<Conversation>>
    suspend fun markMessagesAsRead(conversationId: String, userId: String)
    suspend fun uploadFile(fileUri: Uri, fileName: String): String
    suspend fun downloadFile(context: Context, fileUrl: String)
    suspend fun createConversation(patientId: String, doctorId: String): String
}
