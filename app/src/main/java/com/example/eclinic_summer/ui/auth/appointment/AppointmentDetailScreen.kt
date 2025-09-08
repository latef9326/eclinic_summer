package com.example.eclinic_summer.ui.auth.appointment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.viewmodel.AppointmentDetailViewModel

/**
 * Screen that displays detailed information about a specific appointment.
 * Shows appointment details including date, time, status, and related medical information.
 *
 * @param navController Navigation controller for handling back navigation
 * @param appointmentId The unique identifier of the appointment to display
 * @param viewModel ViewModel that handles appointment detail data loading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    navController: NavController,
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = hiltViewModel()
) {
    val appointment by viewModel.appointment.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load appointment details when the screen is displayed or when appointmentId changes
    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    LoadingIndicator()
                }
                error != null -> {
                    ErrorMessage(error!!)
                }
                appointment == null -> {
                    AppointmentNotFoundMessage()
                }
                else -> {
                    AppointmentDetailCard(appointment!!)
                }
            }
        }
    }
}

/**
 * Displays a loading indicator centered in the available space.
 */
@Composable
fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Displays an error message when appointment loading fails.
 *
 * @param error The exception that occurred during appointment loading
 */
@Composable
fun ErrorMessage(error: Throwable) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("Error: ${error.message}", color = MaterialTheme.colorScheme.error)
    }
}

/**
 * Displays a message when the requested appointment cannot be found.
 */
@Composable
fun AppointmentNotFoundMessage() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("Appointment not found")
    }
}

/**
 * Card component that displays comprehensive appointment details in a structured layout.
 *
 * @param appointment The appointment object containing all details to display
 */
@Composable
fun AppointmentDetailCard(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display all core appointment information
            DetailRow("Appointment ID:", appointment.appointmentId)
            DetailRow("Date:", appointment.date)
            DetailRow("Time:", appointment.time)
            DetailRow("Status:", appointment.status)
            DetailRow("Patient ID:", appointment.patientId)
            DetailRow("Doctor ID:", appointment.doctorId)

            // Conditionally display optional medical information
            if (!appointment.doctorNotes.isNullOrEmpty()) {
                DetailRow("Doctor Notes:", appointment.doctorNotes!!)
            }

            if (!appointment.prescriptionUrl.isNullOrEmpty()) {
                DetailRow("Prescription:", appointment.prescriptionUrl!!)
            }

            if (!appointment.testResultsUrl.isNullOrEmpty()) {
                DetailRow("Test Results:", appointment.testResultsUrl!!)
            }
        }
    }
}

/**
 * Displays a single row of appointment detail information with label and value.
 *
 * @param label The descriptive label for the information
 * @param value The actual value to display
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}