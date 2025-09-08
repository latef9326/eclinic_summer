package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

/**
 * Represents a conversation between a doctor and a patient.
 *
 * @property conversationId Unique identifier for the conversation.
 * @property patientId ID of the patient participant.
 * @property doctorId ID of the doctor participant.
 * @property patientName Name of the patient.
 * @property doctorName Name of the doctor.
 * @property appointmentId ID of the related appointment.
 * @property lastMessage The most recent message in the conversation.
 * @property lastMessageTimestamp Timestamp of the last message.
 * @property participants List of user IDs participating in the conversation.
 * @property unreadCount Number of unread messages in the conversation.
 */
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
