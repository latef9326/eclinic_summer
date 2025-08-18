package com.example.eclinic_summer.data.model.repository

import android.util.Log
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUser(uid: String): User? {
        return try {
            Log.d("UserRepository", "Fetching user data for UID: $uid")
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toUser()
            Log.d("UserRepository", "User data: $user")
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user: ${e.message}", e)
            null
        }
    }

    override suspend fun getUsersByRole(role: String): List<User> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", role)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { it.toUser() }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting users by role: $role", e)
            emptyList()
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteUser(uid: String): Boolean {
        return try {
            firestore.collection("users")
                .document(uid)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user: ${e.message}", e)
            false
        }
    }

    // Dodawanie nowego slotu dostępności
    override suspend fun updateUserAvailability(
        userId: String,
        availability: Availability
    ): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("availability", FieldValue.arrayUnion(availability))
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating availability: ${e.message}", e)
            false
        }
    }

    // Usuwanie istniejącego slotu dostępności
    override suspend fun removeAvailability(
        userId: String,
        availability: Availability
    ): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("availability", FieldValue.arrayRemove(availability))
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error removing availability: ${e.message}", e)
            false
        }
    }

    // Aktualizacja slotu (usuń stary, dodaj nowy)
    override suspend fun updateAvailability(
        userId: String,
        oldSlot: Availability,
        newSlot: Availability
    ): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("availability", FieldValue.arrayRemove(oldSlot))
                .await()
            firestore.collection("users")
                .document(userId)
                .update("availability", FieldValue.arrayUnion(newSlot))
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating availability: ${e.message}", e)
            false
        }
    }

    // 🔹 Poprawiona konwersja DocumentSnapshot do User
    private fun DocumentSnapshot.toUser(): User {
        return User(
            uid = id,
            email = getString("email") ?: "",
            fullName = getString("fullName") ?: "",
            role = getString("role") ?: "patient",
            specialization = getString("specialization"),
            availability = try {
                val availabilityList = get("availability") as? List<*>
                availabilityList?.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> Availability(
                            date = item["date"] as? String ?: "",
                            dayOfWeek = item["dayOfWeek"] as? String ?: "",
                            startTime = item["startTime"] as? String ?: "",
                            endTime = item["endTime"] as? String ?: "",
                            isBooked = item["isBooked"] as? Boolean ?: false
                        )
                        else -> null
                    }
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
}
