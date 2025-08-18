package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

data class Conversation(
    val conversationId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val appointmentId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val participants: List<String> = emptyList(),
    val unreadCount: Int = 0
)