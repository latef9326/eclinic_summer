package com.example.eclinic_summer.domain.domainrepository

import com.example.eclinic_summer.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): FirebaseUser?
    suspend fun register(email: String, password: String, fullName: String, role: String): FirebaseUser?
    suspend fun createUserWithEmailAndPassword(email: String, password: String): String
    suspend fun registerUser(email: String, password: String): Result<AuthResult>
    suspend fun loginUser(email: String, password: String): Result<AuthResult>
    suspend fun saveUserToFirestore(user: User): Result<Void?>
    fun getCurrentUser(): FirebaseUser?
    fun getCurrentUserId(): String?
    fun logout()
}
