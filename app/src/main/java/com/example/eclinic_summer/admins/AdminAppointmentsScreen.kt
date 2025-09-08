package com.example.eclinic_summer.admins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.viewmodel.AdminAppointmentsViewModel

/**
 * Main screen for administrators to manage appointments in the eClinic application.
 * Displays a list of all appointments with options to cancel or view details.
 *
 * @param navController Navigation controller for handling screen transitions
 * @param viewModel ViewModel that handles appointment data and business logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    navController: NavController,
    viewModel: AdminAppointmentsViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load appointments when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadAllAppointments()
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Appointments") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> {
                    LoadingIndicator()
                }
                error != null -> {
                    ErrorMessage(error!!) {
                        viewModel.loadAllAppointments()
                    }
                }
                appointments.isEmpty() -> {
                    EmptyAppointmentsMessage()
                }
                else -> {
                    AppointmentsList(
                        appointments = appointments,
                        onCancel = { appointmentId ->
                            viewModel.cancelAppointment(appointmentId)
                        },
                        onDetails = { appointmentId ->
                            navController.navigate("appointment_detail/$appointmentId")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Displays a loading indicator centered on the screen.
 */
@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

/**
 * Displays a list of appointments in a scrollable column.
 *
 * @param appointments List of appointments to display
 * @param onCancel Callback function when cancel button is clicked
 * @param onDetails Callback function when details button is clicked
 */
@Composable
fun AppointmentsList(
    appointments: List<Appointment>,
    onCancel: (String) -> Unit,
    onDetails: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(appointments) { appointment ->
            AdminAppointmentItem(
                appointment = appointment,
                onCancel = { onCancel(appointment.appointmentId) },
                onDetails = { onDetails(appointment.appointmentId) }
            )
        }
    }
}

/**
 * Individual appointment item card displaying appointment information and action buttons.
 *
 * @param appointment The appointment data to display
 * @param onCancel Callback function triggered when cancel button is clicked
 * @param onDetails Callback function triggered when details button is clicked
 */
@Composable
fun AdminAppointmentItem(
    appointment: Appointment,
    onCancel: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Date: ${appointment.date}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Time: ${appointment.time}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Status: ${appointment.status}",
                color = when (appointment.status) {
                    "scheduled" -> MaterialTheme.colorScheme.primary
                    "completed" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDetails) {
                    Text("Details")
                }
            }
        }
    }
}

/**
 * Displays an error message with a retry button.
 *
 * @param error The exception that occurred
 * @param onRetry Callback function triggered when retry button is clicked
 */
@Composable
fun ErrorMessage(error: Throwable, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error: ${error.message}", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Displays a message when there are no appointments available.
 */
@Composable
fun EmptyAppointmentsMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No appointments available")
    }
}