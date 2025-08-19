package com.example.eclinic_summer.data.model.repository

import android.util.Log
import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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

    // 🔹 Aktualizacja istniejącego slotu dostępności (poprawiona wersja)
    override suspend fun updateAvailability(
        userId: String,
        slotId: String,
        newSlot: Availability
    ): Boolean {
        return try {
            val userRef = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")

                val currentAvailability = user.availability?.toMutableList() ?: mutableListOf()

                // DEBUG logi
                Timber.d("Looking for slotId: $slotId")
                Timber.d("Available slots: ${currentAvailability.map { it.id }}")

                val index = currentAvailability.indexOfFirst { it.id == slotId }

                if (index == -1) {
                    Timber.e("Slot not found. Available IDs: ${currentAvailability.map { it.id }}")
                    throw Exception("Availability slot not found")
                }

                currentAvailability[index] = newSlot
                transaction.update(userRef, "availability", currentAvailability)
            }.await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating availability for user: $userId, slot: $slotId")
            false
        }
    }

    // 🔹 Konwersja DocumentSnapshot → User (uwzględnia Availability z ID)
    private fun DocumentSnapshot.toUser(): User? {
        return try {
            val data = this.data ?: return null
            User(
                uid = this.id,
                email = getString("email") ?: "",
                fullName = getString("fullName") ?: "",
                role = getString("role") ?: "patient",
                phone = getString("phone"),
                address = getString("address"),
                dateOfBirth = getString("dateOfBirth"),
                pesel = getString("pesel"),
                medicalHistory = getString("medicalHistory"),
                specialization = getString("specialization"),
                licenseNumber = getString("licenseNumber"),
                availability = convertToAvailabilityList(get("availability")),
                fcmToken = getString("fcmToken"),
                createdAt = getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                updatedAt = getTimestamp("updatedAt") ?: com.google.firebase.Timestamp.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting document to User")
            null
        }
    }

    private fun convertToAvailabilityList(availabilityData: Any?): List<Availability> {
        return try {
            val list = availabilityData as? List<Map<String, Any>> ?: emptyList()
            list.map { map ->
                Availability(
                    id = (map["id"] as? String) ?: "",
                    date = (map["date"] as? String) ?: "",
                    dayOfWeek = (map["dayOfWeek"] as? String) ?: "",
                    startTime = (map["startTime"] as? String) ?: "",
                    endTime = (map["endTime"] as? String) ?: "",
                    isBooked = (map["isBooked"] as? Boolean) ?: false
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error converting availability data")
            emptyList()
        }
    }
}
