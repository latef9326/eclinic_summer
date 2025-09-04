package com.example.eclinic_summer.ui.auth.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eclinic_summer.data.model.Message
import com.example.eclinic_summer.ui.auth.components.getFileNameFromUri

import com.example.eclinic_summer.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    conversationId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val currentUserId by remember { derivedStateOf { viewModel.getCurrentUserId() } }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = context.contentResolver.getFileNameFromUri(uri)
                viewModel.sendFileMessage(conversationId, uri, fileName)
            }
        }
    )

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        viewModel.markMessagesAsRead(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment button
                IconButton(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    }
                ) {
                    Icon(Icons.Filled.AttachFile, "Attach file")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendTextMessage(
                                conversationId = conversationId,
                                text = messageText
                            )
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(Icons.Filled.Send, "Send message")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = listState
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isCurrentUser = message.senderId == currentUserId,
                    onFileClick = { fileUrl ->
                        // Handle file click (open/download)
                        viewModel.downloadFile(fileUrl)
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onFileClick: (String) -> Unit
) {
    val bubbleColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (message.text != null) {
                    Text(message.text)
                }

                if (message.fileUrl != null) {
                    when (message.type) {
                        "image" -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(message.fileUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clickable { onFileClick(message.fileUrl) }
                            )
                        }
                        "pdf" -> {
                            FileMessageItem(
                                fileName = message.fileName ?: "Document",
                                fileType = "PDF",
                                onClick = { onFileClick(message.fileUrl) }
                            )
                        }
                        else -> {
                            FileMessageItem(
                                fileName = message.fileName ?: "File",
                                fileType = "File",
                                onClick = { onFileClick(message.fileUrl) }
                            )
                        }
                    }
                }

                Text(
                    text = message.timestamp.toDate().formatAsDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FileMessageItem(
    fileName: String,
    fileType: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (fileType) {
                    "PDF" -> Icons.Filled.PictureAsPdf
                    else -> Icons.Filled.AttachFile
                },
                contentDescription = fileType
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = fileName, style = MaterialTheme.typography.bodyMedium)
                Text(text = fileType, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// Extension function to format date and time
fun Date.formatAsDateTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(this)
}