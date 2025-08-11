package com.example.eclinic_summer.ui.auth.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.ui.auth.components.AppointmentCard
import com.example.eclinic_summer.viewmodel.PatientAppointmentViewModel
import kotlinx.coroutines.delay

@Composable
fun BookAppointmentScreen(
    navController: NavController,
    doctorId: String,
    viewModel: PatientAppointmentViewModel = hiltViewModel()
) {
    // Obserwowanie stanów z ViewModel
    val availability by viewModel.availability.collectAsState()
    val bookingStatus by viewModel.bookingStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    // Efekt dla wyboru lekarza
    LaunchedEffect(doctorId) {
        viewModel.selectDoctor(doctorId)
    }

    // Obsługa błędów autentykacji
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

    // Główny interfejs użytkownika
    Box(modifier = Modifier.fillMaxSize()) {
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
                        items(availability.filter { !it.isBooked }) { slot ->
                            AppointmentCard(
                                slot = slot,
                                onBook = { viewModel.book(slot) }
                            )
                        }
                    }
                }

                // Obsługa statusu rezerwacji
                bookingStatus?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    if (result.isSuccess) {
                        LaunchedEffect(Unit) {
                            delay(2000)
                            viewModel.resetErrors()
                        }
                        Text(
                            text = "Booked successfully!",
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Error: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}