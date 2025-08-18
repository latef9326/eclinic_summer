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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, Dr. ${currentUser?.fullName ?: ""}!",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1) Zarządzanie harmonogramem (z przekazaniem doctorId)
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

                    // 2) Lista wizyt
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

                    // W obu dashboardach zmień:
                    Button(
                        onClick = {
                            currentUser?.uid?.let {
                                navController.navigate("chat_list/$it") // POPRAWIONE: dodano argument
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("My Chats")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4) Dokumenty medyczne
                    Button(
                        onClick = {
                            currentUser?.uid?.let {
                                navController.navigate("documents/$it")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("My Documents")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5) Konsultacje online (placeholder)
                    Button(
                        onClick = { /* TODO: Implement online consultations */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Online Consultations")
                    }
                }

                // Logout w prawym dolnym rogu
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("doctor_dashboard") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Logout")
                }
            }
        }
    )
}
