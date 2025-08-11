package com.example.eclinic_summer.admins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val doctors by viewModel.doctors.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Doctors", "Patients")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") })
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("add_doctor") }
                ) {
                    Icon(Icons.Default.Add, "Add Doctor")
                }
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                when {
                    isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Error: ${error?.message}")
                    }
                    selectedTab == 0 && doctors.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No doctors found")
                    }
                    selectedTab == 1 && patients.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No patients found")
                    }
                    selectedTab == 0 -> DoctorList(
                        doctors = doctors,
                        onEdit = { doctorId ->
                            navController.navigate("edit_doctor/$doctorId")
                        },
                        onDelete = { doctorId ->
                            viewModel.deleteUser(doctorId)
                        }
                    )
                    selectedTab == 1 -> PatientList(
                        patients = patients,
                        onDelete = { patientId ->
                            viewModel.deleteUser(patientId)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun DoctorList(
    doctors: List<User>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(doctors) { doctor ->
            DoctorItem(
                doctor = doctor,
                onEdit = { onEdit(doctor.uid) },
                onDelete = { onDelete(doctor.uid) }
            )
        }
    }
}

@Composable
fun DoctorItem(
    doctor: User,
    onEdit: () -> Unit,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = doctor.fullName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = doctor.specialization ?: "General Practitioner",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = doctor.email,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit Doctor")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete Doctor")
                }
            }
        }
    }
}

@Composable
fun PatientList(
    patients: List<User>,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(patients) { patient ->
            PatientItem(
                patient = patient,
                onDelete = { onDelete(patient.uid) }
            )
        }
    }
}

@Composable
fun PatientItem(
    patient: User,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = patient.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete Patient")
            }
        }
    }
}