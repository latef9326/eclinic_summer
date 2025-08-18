package com.example.eclinic_summer.data.model.repository

import android.net.Uri
import com.example.eclinic_summer.data.model.Document
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : DocumentRepository {

    private val documentsRef = firestore.collection("documents")
    private val storageRef = storage.reference.child("user_documents")

    override suspend fun uploadDocument(fileUri: Uri, ownerId: String): Document {
        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val fileRef = storageRef.child(fileName)
        fileRef.putFile(fileUri).await()
        val url = fileRef.downloadUrl.await().toString()

        val document = Document(
            ownerId = ownerId,
            name = fileUri.lastPathSegment ?: "Document",
            url = url,
            type = fileUri.getFileType(),
            uploadedAt = Timestamp.now()
        )

        documentsRef.add(document).await()
        return document
    }

    override fun getDocuments(ownerId: String): Flow<List<Document>> {
        return documentsRef
            .whereEqualTo("ownerId", ownerId)
            .orderBy("uploadedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.documents.map { it.toObject(Document::class.java)!! } }
    }

    override suspend fun deleteDocument(documentId: String) {
        val doc = documentsRef.document(documentId).get().await()
        val document = doc.toObject(Document::class.java)

        documentsRef.document(documentId).delete().await()

        document?.url?.let { url ->
            storage.getReferenceFromUrl(url).delete().await()
        }
    }

    override suspend fun downloadDocument(url: String) {
        try {
            val fileRef = storage.getReferenceFromUrl(url)
            val fileName = url.substringAfterLast("/")
            val localFile = java.io.File.createTempFile(fileName, null)
            fileRef.getFile(localFile).await()
            println("File downloaded to: ${localFile.absolutePath}")
        } catch (e: Exception) {
            throw e
        }
    }
}

private fun Uri.getFileType(): String {
    val extension = this.toString().substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif" -> "image"
        "pdf" -> "pdf"
        "doc", "docx" -> "document"
        else -> "file"
    }
}