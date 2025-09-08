// ProfileScreen.kt
package com.example.eclinic_summer.ui.auth.profile

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
import com.example.eclinic_summer.viewmodel.ProfileViewModel

/**
 * Screen for viewing and editing user profile information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val specialization by viewModel.specialization.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val address by viewModel.address.collectAsState()
    val dateOfBirth by viewModel.dateOfBirth.collectAsState()
    val pesel by viewModel.pesel.collectAsState()
    val licenseNumber by viewModel.licenseNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(userId) { viewModel.loadUser(userId) }
    LaunchedEffect(isSuccess) { if (isSuccess) navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (user == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("User not found")
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

                if (user?.role == "doctor") {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = viewModel::onSpecializationChange,
                        label = { Text("Specialization") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = viewModel::onLicenseNumberChange,
                        label = { Text("License Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = viewModel::onAddressChange,
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = viewModel::onDateOfBirthChange,
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pesel,
                    onValueChange = viewModel::onPeselChange,
                    label = { Text("PESEL") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = "Error: ${error?.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { viewModel.updateUser() },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Profile")
                    }
                }
            }
        }
    }
}
