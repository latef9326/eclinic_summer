package com.example.eclinic_summer.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eclinic_summer.data.model.Document
import com.example.eclinic_summer.data.model.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing documents for a user.
 * Supports loading, uploading, deleting, and downloading documents.
 */
@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    /** List of documents for the current user or owner. */
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    /** Tracks the current owner of the documents for refresh purposes. */
    private var currentOwnerId: String = ""

    /**
     * Loads all documents for a given owner ID.
     */
    fun loadDocuments(ownerId: String) {
        currentOwnerId = ownerId
        viewModelScope.launch {
            documentRepository.getDocuments(ownerId).collect {
                _documents.value = it
            }
        }
    }

    /**
     * Uploads a new document for the specified owner.
     */
    fun uploadDocument(fileUri: Uri, ownerId: String) {
        viewModelScope.launch {
            documentRepository.uploadDocument(fileUri, ownerId)
            loadDocuments(ownerId) // Refresh the document list
        }
    }

    /**
     * Deletes a document by its ID.
     */
    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            documentRepository.deleteDocument(documentId)
            loadDocuments(currentOwnerId) // Refresh the document list
        }
    }

    /**
     * Downloads a document given its URL.
     */
    fun downloadDocument(document: Document) {
        viewModelScope.launch {
            documentRepository.downloadDocument(document.url)
        }
    }
}
