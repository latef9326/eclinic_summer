package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

/**
 * Represents a single message in a conversation.
 *
 * @property messageId Unique identifier of the message.
 * @property conversationId ID of the conversation this message belongs to.
 * @property senderId ID of the user who sent the message.
 * @property receiverId ID of the recipient.
 * @property text Optional text content of the message.
 * @property fileUrl Optional URL if the message contains a file.
 * @property fileName Optional name of the attached file.
 * @property timestamp Timestamp of when the message was sent.
 * @property type Type of message ("text", "image", "pdf", "file").
 * @property read Whether the message has been read by the receiver.
 */
data class Message(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text",
    val read: Boolean = false
)
