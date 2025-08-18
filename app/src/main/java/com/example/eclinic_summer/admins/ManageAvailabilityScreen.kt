package com.example.eclinic_summer.admins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.viewmodel.AvailabilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAvailabilityScreen(
    doctorId: String,
    navController: NavController,
    viewModel: AvailabilityViewModel = hiltViewModel()
) {
    val availability by viewModel.availability.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(doctorId) {
        viewModel.loadAvailability(doctorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Availability") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${error?.message}", color = MaterialTheme.colorScheme.error)
                }
            } else if (availability.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No availability slots found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(availability) { slot ->
                        AvailabilitySlotItem(
                            slot = slot,
                            onDelete = { viewModel.deleteSlot(doctorId, slot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilitySlotItem(
    slot: Availability,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date: ${slot.date}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Time: ${slot.startTime} - ${slot.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (slot.isBooked) "Booked" else "Available",
                    color = if (slot.isBooked) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete Slot")
            }
        }
    }
}