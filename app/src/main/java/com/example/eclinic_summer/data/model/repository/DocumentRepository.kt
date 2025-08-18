package com.example.eclinic_summer.data.model.repository

import android.net.Uri
import com.example.eclinic_summer.data.model.Document
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun uploadDocument(fileUri: Uri, ownerId: String): Document
    fun getDocuments(ownerId: String): Flow<List<Document>>
    suspend fun deleteDocument(documentId: String)
    suspend fun downloadDocument(url: String)
}