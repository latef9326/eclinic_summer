package com.example.eclinic_summer.ui.auth.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.User

/**
 * Screen displaying a searchable list of users (doctors or patients) for chat initiation.
 *
 * The content of the list depends on the role of the current user:
 * - If the current user is a patient → list of doctors.
 * - If the current user is a doctor → list of patients.
 *
 * @param navController Navigation controller used to handle navigation between screens.
 * @param userId The ID of the currently logged-in user.
 * @param viewModel The [ChatListViewModel] injected via Hilt.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    userId: String,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val users by viewModel.filteredUsers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val error by viewModel.error.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Load users for the given role on initial composition
    LaunchedEffect(userId) {
        viewModel.loadUsers(userId)
    }

    // Dynamic title depending on the user role
    val title = remember(users) {
        if (users.isNotEmpty()) {
            if (users[0].role == "doctor") "Doctors" else "Patients"
        } else {
            "Chats"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Search $title...") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                null,
                                modifier = Modifier.clickable {
                                    keyboardController?.hide()
                                }
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (users.isEmpty() && error == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No $title available", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(users) { user ->
                        UserChatItem(
                            user = user,
                            onClick = {
                                viewModel.createConversation(userId, user.uid) { conversationId ->
                                    navController.navigate("chat/$conversationId")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Represents a single user item in the chat list.
 *
 * @param user The user (doctor or patient) to be displayed.
 * @param onClick Callback triggered when the item is clicked.
 */
@Composable
fun UserChatItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium
                )

                // If doctor → show specialization, else → show email
                if (user.role == "doctor") {
                    Text(
                        text = user.specialization ?: "General Practitioner",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = user.email ?: "Patient",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
