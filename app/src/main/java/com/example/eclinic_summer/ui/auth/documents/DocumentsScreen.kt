package com.example.eclinic_summer.ui.auth.documents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.Document
import com.example.eclinic_summer.viewmodel.DocumentsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    navController: NavController,
    userId: String,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val documents by viewModel.documents.collectAsState(initial = emptyList())

    LaunchedEffect(userId) {
        viewModel.loadDocuments(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Documents") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Otwórz selektor plików */ }
            ) {
                Icon(Icons.Filled.Add, "Add document")
            }
        }
    ) { padding ->
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No documents yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(documents) { document ->
                    DocumentItem(
                        document = document,
                        onDownload = { viewModel.downloadDocument(document) },
                        onDelete = { viewModel.deleteDocument(document.documentId) }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    document: Document,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = document.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall
                )
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Text(
                    text = sdf.format(document.uploadedAt.toDate()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                Button(onClick = onDownload, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Download")
                }
                Button(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

