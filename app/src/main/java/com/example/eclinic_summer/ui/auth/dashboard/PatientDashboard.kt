package com.example.eclinic_summer.ui.auth.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboard(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Automatyczne przekierowanie jeśli użytkownik nie jest zalogowany
    LaunchedEffect(currentUser) {
        if (currentUser == null && !isLoading) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
    }

    if (isLoading || currentUser == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Patient Dashboard") })
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
                        text = "Welcome, ${currentUser?.fullName ?: "Patient"}!",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            currentUser?.uid?.let { uid ->
                                navController.navigate("doctors/$uid") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } ?: run {
                                // Jeśli UID jest nullem, wymuś ponowne pobranie danych
                                authViewModel.fetchUserData(
                                    onError = {
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Book Appointment")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            currentUser?.uid?.let {
                                navController.navigate("medical_history/$it")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Medical History")
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



                    Spacer(modifier = Modifier.height(16.dp))

                    // DODANY PRZYCISK PROFILU - w odpowiednim miejscu
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

                    // Logout na dole, ale w Column
                    Button(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("patient_dashboard") { inclusive = true }
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
}