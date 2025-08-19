package com.example.eclinic_summer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eclinic_summer.admins.AddDoctorScreen
import com.example.eclinic_summer.admins.AdminDashboard
import com.example.eclinic_summer.admins.ManageAvailabilityScreen
import com.example.eclinic_summer.ui.auth.LoginScreen
import com.example.eclinic_summer.ui.auth.RegisterScreen
import com.example.eclinic_summer.ui.auth.appointment.AppointmentDetailScreen
import com.example.eclinic_summer.ui.auth.appointment.BookAppointmentScreen
import com.example.eclinic_summer.ui.auth.appointment.MedicalHistoryScreen
import com.example.eclinic_summer.ui.auth.appointment.ScheduleEditorScreen
import com.example.eclinic_summer.ui.auth.chat.ChatListScreen
import com.example.eclinic_summer.ui.auth.chat.ChatScreen
import com.example.eclinic_summer.ui.auth.dashboard.PatientDashboard
import com.example.eclinic_summer.ui.auth.doctors.DoctorAppointmentsScreen
import com.example.eclinic_summer.ui.auth.doctors.DoctorListScreen
import com.example.eclinic_summer.ui.auth.doctors.DoctorDashboard
import com.example.eclinic_summer.ui.auth.documents.DocumentsScreen
import com.example.eclinic_summer.ui.auth.profile.ProfileScreen
import com.example.eclinic_summer.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sprawdź i poproś o uprawnienia do powiadomień (dla Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }

        // Pobierz token FCM dla tego urządzenia
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Timber.d("🔥 Current FCM Token: $token")

            // Zapisz token w Firestore
            val user = Firebase.auth.currentUser
            user?.let {
                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Timber.d("FCM token saved to Firestore")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error saving FCM token")
                    }
            }
        }

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
        "admin" -> "admin_dashboard"
        "doctor" -> "doctor_dashboard"
        "patient" -> "patient_dashboard"
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Profile
        composable(
            "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(navController, userId)
        }

        // Auth Screens
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        // Dashboards
        composable("patient_dashboard") { PatientDashboard(navController) }
        composable("doctor_dashboard") { DoctorDashboard(navController) }
        composable("admin_dashboard") { AdminDashboard(navController) }

        // Doctors
        composable(
            "doctors/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            DoctorListScreen(navController, patientId)
        }

        // Appointments
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
        ) { DoctorAppointmentsScreen(navController) }

        // Admin screens
        composable("add_doctor") { AddDoctorScreen(navController) }

        composable(
            "manage_availability/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            ManageAvailabilityScreen(
                doctorId = doctorId,
                navController = navController
            )
        }

        // Appointment Detail
        composable(
            "appointment_detail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AppointmentDetailScreen(navController, appointmentId)
        }

        // Medical History
        composable(
            "medical_history/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            MedicalHistoryScreen(navController, patientId)
        }

        // Chat
        composable(
            "chat_list/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChatListScreen(navController, userId)
        }

        composable(
            "chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                navController = navController,
                conversationId = conversationId,
                viewModel = hiltViewModel()
            )
        }

        // Documents
        composable(
            "documents/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DocumentsScreen(navController, userId)
        }
    }
}