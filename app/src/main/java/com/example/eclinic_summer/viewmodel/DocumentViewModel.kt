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

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    private var currentOwnerId: String = ""

    fun loadDocuments(ownerId: String) {
        currentOwnerId = ownerId
        viewModelScope.launch {
            documentRepository.getDocuments(ownerId).collect {
                _documents.value = it
            }
        }
    }

    fun uploadDocument(fileUri: Uri, ownerId: String) {
        viewModelScope.launch {
            documentRepository.uploadDocument(fileUri, ownerId)
            loadDocuments(ownerId)
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            documentRepository.deleteDocument(documentId)
            loadDocuments(currentOwnerId)
        }
    }

    fun downloadDocument(document: Document) {
        viewModelScope.launch {
            documentRepository.downloadDocument(document.url)
        }
    }
}
