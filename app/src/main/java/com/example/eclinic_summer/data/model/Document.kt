package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

data class Document(
    val documentId: String = "",
    val ownerId: String = "", // user who uploaded
    val name: String = "",
    val url: String = "",
    val uploadedAt: Timestamp = Timestamp.now(),
    val type: String = "prescription" // or "report", "image", etc.
)
