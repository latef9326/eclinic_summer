package com.example.eclinic_summer.ui.auth.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.viewmodel.DoctorScheduleViewModel

@Composable
fun ScheduleEditorScreen(
    navController: NavController,
    doctorId: String,
    viewModel: DoctorScheduleViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<Availability?>(null) }
    val availability by viewModel.availability.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(doctorId) {
        viewModel.loadAvailability(doctorId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Your Availability", style = MaterialTheme.typography.headlineMedium)

        when {
            isLoading -> CircularProgressIndicator()
            availability.isEmpty() -> Text("No availability slots added")
            else -> {
                LazyColumn {
                    items(availability) { slot ->
                        AvailabilitySlotItem(
                            slot = slot,
                            onEdit = {
                                editingSlot = slot
                                showDialog = true
                            },
                            onDelete = {
                                viewModel.deleteSlot(doctorId, slot)
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                editingSlot = null
                showDialog = true
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add New Slot")
        }
    }

    if (showDialog) {
        AvailabilitySlotDialog(
            initialSlot = editingSlot,
            onDismiss = { showDialog = false },
            onSave = { slot ->
                if (editingSlot == null) {
                    viewModel.addNewSlot(doctorId, slot)
                } else {
                    viewModel.updateSlot(doctorId, editingSlot!!, slot)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun AvailabilitySlotItem(
    slot: Availability,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("${slot.dayOfWeek}: ${slot.startTime}-${slot.endTime}")
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun AvailabilitySlotDialog(
    initialSlot: Availability?,
    onDismiss: () -> Unit,
    onSave: (Availability) -> Unit
) {
    var date by remember { mutableStateOf(initialSlot?.date ?: "") }
    var dayOfWeek by remember { mutableStateOf(initialSlot?.dayOfWeek ?: "") }
    var startTime by remember { mutableStateOf(initialSlot?.startTime ?: "") }
    var endTime by remember { mutableStateOf(initialSlot?.endTime ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSlot == null) "Add New Slot" else "Edit Slot") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dayOfWeek,
                    onValueChange = { dayOfWeek = it },
                    label = { Text("Day of Week") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (e.g., 14 for 2 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (e.g., 15 for 3 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val slot = Availability(
                    date = date,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    isBooked = initialSlot?.isBooked ?: false
                )
                onSave(slot)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
