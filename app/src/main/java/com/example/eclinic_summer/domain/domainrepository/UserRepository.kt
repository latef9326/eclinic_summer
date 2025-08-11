package com.example.eclinic_summer.domain.domainrepository

import com.example.eclinic_summer.data.model.User
import com.example.eclinic_summer.data.model.repository.Availability

interface UserRepository {
    suspend fun getUser(uid: String): User?
    suspend fun getUsersByRole(role: String): List<User>
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(uid: String): Boolean

    // Dodane funkcje do zarządzania dostępnością
    suspend fun updateUserAvailability(
        userId: String,
        availability: Availability
    ): Boolean

    suspend fun removeAvailability(
        userId: String,
        availability: Availability
    ): Boolean

    suspend fun updateAvailability(
        userId: String,
        oldSlot: Availability,
        newSlot: Availability
    ): Boolean
}
