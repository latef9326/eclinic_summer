package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

/**
 * Represents a document uploaded by a user (e.g., prescription, test results).
 *
 * @property documentId Unique identifier of the document.
 * @property ownerId ID of the user who uploaded the document.
 * @property name File name of the document.
 * @property url Publicly accessible URL to the document.
 * @property type Type of the document (e.g., "pdf", "image").
 * @property uploadedAt Timestamp when the document was uploaded.
 */
data class Document(
    val documentId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val url: String = "",
    val type: String = "",
    val uploadedAt: Timestamp = Timestamp.now()
)
