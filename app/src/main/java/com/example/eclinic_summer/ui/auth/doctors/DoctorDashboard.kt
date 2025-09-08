package com.example.eclinic_summer.ui.auth.doctors

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.viewmodel.AuthViewModel

/**
 * Dashboard screen for doctors.
 *
 * Provides navigation to manage schedule, view appointments, chats,
 * profile, and logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboard(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Doctor Dashboard") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome, Dr. ${currentUser?.fullName ?: ""}!",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        currentUser?.uid?.let { uid ->
                            navController.navigate("schedule/$uid")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Schedule")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        currentUser?.uid?.let { uid ->
                            navController.navigate("appointments/$uid")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Appointments")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        currentUser?.uid?.let {
                            navController.navigate("chat_list/$it")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("My Chats")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        currentUser?.uid?.let {
                            navController.navigate("profile/$it")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View/Edit My Profile")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("doctor_dashboard") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    )
}
