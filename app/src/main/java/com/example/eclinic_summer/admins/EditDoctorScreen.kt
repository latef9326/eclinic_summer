package com.example.eclinic_summer.admins

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.viewmodel.EditDoctorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDoctorScreen(
    navController: NavController,
    doctorId: String,
    viewModel: EditDoctorViewModel = hiltViewModel()
) {
    val doctor by viewModel.doctor.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val specialization by viewModel.specialization.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(doctor) {
        if (doctor != null) {
            viewModel.onFullNameChange(doctor!!.fullName)
            viewModel.onEmailChange(doctor!!.email)
            viewModel.onSpecializationChange(doctor!!.specialization ?: "")
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Doctor") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (doctor == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Doctor not found")
                }
            } else {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialization,
                    onValueChange = viewModel::onSpecializationChange,
                    label = { Text("Specialization") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = "Error: ${error?.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { viewModel.updateDoctor() },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Doctor")
                    }
                }
            }
        }
    }
}