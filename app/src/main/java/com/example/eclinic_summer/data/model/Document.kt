package com.example.eclinic_summer.data.model

import com.google.firebase.Timestamp

data class Document(
    val documentId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val url: String = "",
    val type: String = "",
    val uploadedAt: Timestamp = Timestamp.now()
)