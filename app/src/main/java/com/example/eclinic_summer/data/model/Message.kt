package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val conversationId: String = "", // ID konwersacji (np. appointmentId)
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text" // "text", "image", "pdf", "prescription"
)