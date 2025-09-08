package com.example.eclinic_summer.ui.auth.doctors

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.viewmodel.DoctorListViewModel

/**
 * Screen displaying a list of doctors for a patient to choose from.
 */
@Composable
fun DoctorListScreen(
    navController: NavController,
    patientId: String,
    viewModel: DoctorListViewModel = hiltViewModel()
) {
    val doctors by viewModel.doctors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Error: ${error?.message}")
        }
    } else if (doctors.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No doctors available")
        }
    } else {
        LazyColumn {
            items(doctors) { doctor ->
                DoctorCard(
                    doctor = doctor,
                    onClick = {
                        navController.navigate("book_appointment/${doctor.uid}")
                    }
                )
            }
        }
    }
}

/**
 * Composable showing a single doctor card.
 */
@Composable
fun DoctorCard(
    doctor: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = doctor.fullName, style = MaterialTheme.typography.titleLarge)
            Text(text = doctor.specialization ?: "General Practitioner", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
