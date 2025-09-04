package com.example.eclinic_summer.ui.auth.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.viewmodel.PatientAppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    navController: NavController,
    doctorId: String,
    viewModel: PatientAppointmentViewModel = hiltViewModel()
) {
    val availability by viewModel.availability.collectAsState()
    val bookingStatus by viewModel.bookingStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    LaunchedEffect(doctorId) {
        viewModel.selectDoctor(doctorId)
    }

    if (authError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.resetErrors() },
            title = { Text("Authentication Required") },
            text = { Text(authError!!) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetErrors()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Go to Login")
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select a slot to book:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (availability.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No available time slots")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(availability) { slot ->
                                AvailabilitySlotItem(
                                    slot = slot,
                                    onBook = {
                                        if (slot.getStatus() == "available") {
                                            viewModel.book(slot)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    bookingStatus?.let { result ->
                        Spacer(modifier = Modifier.height(16.dp))
                        if (result.isSuccess) {
                            Text(
                                text = "✅ Booked successfully!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "❌ Error: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilitySlotItem(
    slot: Availability,
    onBook: () -> Unit
) {
    val slotStatus = try {
        slot.getStatus()
    } catch (e: Exception) {
        "available" // Domyślnie dostępny w przypadku błędu
    }
    val (statusText, statusColor, buttonText, enabled) = when (slotStatus) {
        "completed" -> listOf(
            "✅ Appointment completed",
            MaterialTheme.colorScheme.primary,
            "Completed",
            false
        )
        "expired" -> listOf(
            "❌ Appointment expired",
            MaterialTheme.colorScheme.error,
            "Expired",
            false
        )
        "scheduled" -> listOf(
            "✅ Scheduled",
            MaterialTheme.colorScheme.primary,
            "Booked",
            false
        )
        else -> listOf(
            "Available",
            MaterialTheme.colorScheme.onSurface,
            "Book Now",
            true
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (slotStatus) {
                "expired" -> MaterialTheme.colorScheme.errorContainer
                "completed" -> MaterialTheme.colorScheme.primaryContainer
                "scheduled" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when (slotStatus) {
            "scheduled", "completed" -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            "expired" -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            else -> null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${slot.date} • ${slot.startTime} - ${slot.endTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (slotStatus) {
                        "expired" -> MaterialTheme.colorScheme.onErrorContainer
                        "completed" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "scheduled" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = statusText.toString(),
                    color = statusColor as androidx.compose.ui.graphics.Color,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onBook,
                enabled = enabled as Boolean,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!(enabled as Boolean))
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(buttonText.toString())
            }
        }
    }
}
