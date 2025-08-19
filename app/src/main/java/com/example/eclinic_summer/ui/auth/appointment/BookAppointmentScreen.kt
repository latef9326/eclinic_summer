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
import com.example.eclinic_summer.ui.auth.components.AppointmentCard
import com.example.eclinic_summer.viewmodel.PatientAppointmentViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    navController: NavController,
    doctorId: String,
    viewModel: PatientAppointmentViewModel = hiltViewModel()
) {
    // Używamy TYLKO availability, nie availabilityState
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
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
                                // Sprawdź czy slot nie jest już zarezerwowany
                                val isAlreadyBooked = bookingStatus?.isSuccess == true &&
                                        viewModel.isSlotBooked(slot.id)

                                AvailabilitySlotItem(
                                    slot = if (isAlreadyBooked) slot.copy(isBooked = true) else slot,
                                    onBook = {
                                        if (!slot.isBooked && !isAlreadyBooked) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isBooked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (slot.isBooked)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else
            null
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
                    color = if (slot.isBooked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (slot.isBooked) {
                    Text(
                        text = "✅ Scheduled",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onBook,
                enabled = !slot.isBooked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (slot.isBooked)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (slot.isBooked) "Booked" else "Book Now")
            }
        }
    }
}
