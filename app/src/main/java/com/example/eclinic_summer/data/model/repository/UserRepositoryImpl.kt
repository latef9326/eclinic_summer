package com.example.eclinic_summer.data.model.repository

import android.util.Log
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.example.eclinic_summer.data.model.repository.Availability
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
            false
        }
    }

    override suspend fun updateUserAvailability(
        userId: String,
        availability: Availability
    ): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(
                    "availability", FieldValue.arrayUnion(availability)
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeAvailability(
        userId: String,
        availability: Availability
    ): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(
                    "availability", FieldValue.arrayRemove(availability)
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateAvailability(
        userId: String,
        oldSlot: Availability,
        newSlot: Availability
    ): Boolean {
        return try {
            // Usuń stary slot
            firestore.collection("users")
                .document(userId)
                .update(
                    "availability", FieldValue.arrayRemove(oldSlot)
                )
                .await()

            // Dodaj nowy slot
            firestore.collection("users")
                .document(userId)
                .update(
                    "availability", FieldValue.arrayUnion(newSlot)
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun DocumentSnapshot.toUser(): User {
        return User(
            uid = id,
            email = getString("email") ?: "",
            fullName = getString("fullName") ?: "",
            role = getString("role") ?: "patient",
            specialization = getString("specialization"),
            availability = try {
                val availabilityList = get("availability") as? List<Map<String, Any>>?
                availabilityList?.map { map ->
                    Availability(
                        date = map["date"] as? String ?: "",
                        dayOfWeek = map["dayOfWeek"] as? String ?: "",
                        startTime = map["startTime"] as? String ?: "",
                        endTime = map["endTime"] as? String ?: "",
                        isBooked = map["isBooked"] as? Boolean ?: false
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
}
