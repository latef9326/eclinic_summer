package com.example.eclinic_summer.data.model.repository

import android.net.Uri
import com.example.eclinic_summer.data.model.Conversation
import com.example.eclinic_summer.data.model.Message
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ChatRepository {

    private val conversationsRef = firestore.collection("conversations")
    private val messagesRef = firestore.collection("messages")
    private val filesRef = storage.reference.child("chat_files")

    override suspend fun sendMessage(message: Message) {
        messagesRef.document().set(message).await()

        conversationsRef.document(message.conversationId)
            .update(
                mapOf(
                    "lastMessage" to (message.text ?: "File: ${message.fileName}"),
                    "lastMessageTimestamp" to message.timestamp
                )
            )
            .await()
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return messagesRef
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp")
            .snapshots()
            .map { snap -> snap.documents.map { it.toObject(Message::class.java)!! } }
    }

    override fun getConversations(userId: String): Flow<List<Conversation>> {
        return conversationsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.documents.map { it.toObject(Conversation::class.java)!! } }
    }

    override suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        // TODO: Implementacja oznaczania wiadomości jako przeczytane
    }

    override suspend fun uploadFile(fileUri: Uri, fileName: String): String {
        val fileRef = filesRef.child("${System.currentTimeMillis()}_$fileName")
        fileRef.putFile(fileUri).await()
        return fileRef.downloadUrl.await().toString()
    }

    // 🔹 Tworzenie konwersacji z unikaniem duplikatów
    override suspend fun createConversation(patientId: String, doctorId: String): String {
        // Najpierw sprawdź czy konwersacja już istnieje
        findExistingConversation(patientId, doctorId)?.let { return it }

        // Pobierz minimalne dane użytkowników (tylko fullName)
        val patientName = try {
            firestore.collection("users").document(patientId).get().await()
                .getString("fullName") ?: "Patient"
        } catch (e: Exception) {
            "Patient"
        }

        val doctorName = try {
            firestore.collection("users").document(doctorId).get().await()
                .getString("fullName") ?: "Doctor"
        } catch (e: Exception) {
            "Doctor"
        }

        val conversation = Conversation(
            patientId = patientId,
            doctorId = doctorId,
            patientName = patientName,
            doctorName = doctorName,
            participants = listOf(patientId, doctorId),
            lastMessage = "Conversation started",
            lastMessageTimestamp = Timestamp.now()
        )

        return conversationsRef.add(conversation).await().id
    }

    // 🔹 Znajdowanie istniejącej konwersacji (limit i optymalizacja)
    private suspend fun findExistingConversation(patientId: String, doctorId: String): String? {
        val querySnapshot = conversationsRef
            .whereArrayContains("participants", patientId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .limit(50) // ograniczenie liczby dokumentów
            .get()
            .await()

        return querySnapshot.documents.firstOrNull { doc ->
            (doc.get("participants") as? List<*>)?.contains(doctorId) == true
        }?.id
    }
}
