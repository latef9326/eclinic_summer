package com.example.eclinic_summer.data.model.repository

import android.util.Log
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): FirebaseUser? {
        return try {
            auth.signInWithEmailAndPassword(email, password).await().user?.also {
                Log.d("AuthRepo", "User logged in: ${it.uid}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login failed", e)
            null
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): FirebaseUser? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.also {
                Log.d("AuthRepo", "User registered: ${it.uid}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Registration failed", e)
            null
        }
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): String {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid ?: throw Exception("User creation failed")
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun registerUser(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<AuthResult> {
        return try {
            Log.d("AuthRepository", "Firebase login attempt: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Firebase login successful. UID: ${result.user?.uid}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firebase login error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun saveUserToFirestore(user: User): Result<Void?> {
        return try {
            val result = firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid.also {
            Log.d("AuthRepo", "Current UID: $it")
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser.also {
            Log.d("AuthRepo", "Current user: ${it?.uid}")
        }
    }

    override fun logout() {
        auth.signOut()
        Log.d("AuthRepo", "User logged out")
    }
}