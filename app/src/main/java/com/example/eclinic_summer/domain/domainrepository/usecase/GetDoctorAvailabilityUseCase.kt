package com.example.eclinic_summer.domain.domainrepository.usecase

import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for retrieving a doctor’s availability slots.
 */
class GetDoctorAvailabilityUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Retrieves the list of availability slots for a given doctor.
     *
     * @param doctorId ID of the doctor.
     * @return List of available slots (empty if none found).
     */
    suspend operator fun invoke(doctorId: String): List<Availability> {
        return try {
            val doctor = userRepository.getUser(doctorId)
            val availability = doctor?.availability ?: emptyList()

            availability.forEach { slot ->
                Timber.d("Slot: ${slot.id}, booked: ${slot.isBooked}, date: ${slot.date}")
            }

            availability
        } catch (e: Exception) {
            Timber.e(e, "Error getting doctor availability")
            emptyList()
        }
    }
}
