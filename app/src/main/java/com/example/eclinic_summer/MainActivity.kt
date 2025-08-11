package com.example.eclinic_summer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eclinic_summer.admins.AddDoctorScreen
import com.example.eclinic_summer.ui.auth.LoginScreen
import com.example.eclinic_summer.ui.auth.RegisterScreen
import com.example.eclinic_summer.admins.AdminDashboard
import com.example.eclinic_summer.ui.auth.appointment.BookAppointmentScreen
import com.example.eclinic_summer.ui.auth.dashboard.PatientDashboard
import com.example.eclinic_summer.ui.auth.appointment.ScheduleEditorScreen
import com.example.eclinic_summer.ui.auth.doctors.DoctorAppointmentsScreen
import com.example.eclinic_summer.admins.EditDoctorScreen
import com.example.eclinic_summer.ui.auth.appointment.MedicalHistoryScreen
import com.example.eclinic_summer.ui.auth.doctors.DoctorListScreen
import com.example.eclinic_summer.ui.auth.doctors.DoctorDashboard
import com.example.eclinic_summer.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EClinicNavigation()
        }
    }
}

@Composable
fun EClinicNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = when (currentUser?.role) {
        "admin" -> {
            Log.d("Navigation", "Start destination: admin_dashboard")
            "admin_dashboard"
        }
        "doctor" -> {
            Log.d("Navigation", "Start destination: doctor_dashboard")
            "doctor_dashboard"
        }
        "patient" -> {
            Log.d("Navigation", "Start destination: patient_dashboard")
            "patient_dashboard"
        }
        else -> {
            Log.d("Navigation", "Start destination: login (no user)")
            "login"
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("patient_dashboard") {
            PatientDashboard(navController)
        }
        composable("doctor_dashboard") {
            DoctorDashboard(navController)
        }
        composable("admin_dashboard") {
            AdminDashboard(navController)
        }
        composable(
            "doctors/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            DoctorListScreen(navController, patientId)
        }
        composable(
            "book_appointment/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            BookAppointmentScreen(navController, doctorId = doctorId)
        }
        composable(
            "schedule/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            ScheduleEditorScreen(navController, doctorId)
        }
        composable(
            "appointments/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) {
            DoctorAppointmentsScreen(navController)
        }
        composable("add_doctor") {
            AddDoctorScreen(navController)
        }
        composable(
            "edit_doctor/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            EditDoctorScreen(navController, doctorId)
        }
        composable(
            "medical_history/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            MedicalHistoryScreen(navController, patientId)
        }
    }
}
