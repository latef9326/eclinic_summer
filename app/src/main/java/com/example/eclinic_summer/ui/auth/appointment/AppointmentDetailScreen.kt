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
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Error: ${error?.message}", color = MaterialTheme.colorScheme.error)
                    }
                } else if (appointment == null) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Appointment not found")
                    }
                } else {
                    AppointmentDetailCard(appointment!!)
                }
            }
        }
    }

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
                DetailRow("Appointment ID:", appointment.appointmentId)
                DetailRow("Date:", appointment.date)
                DetailRow("Time:", appointment.time)
                DetailRow("Status:", appointment.status)
                DetailRow("Patient ID:", appointment.patientId)
                DetailRow("Doctor ID:", appointment.doctorId)

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