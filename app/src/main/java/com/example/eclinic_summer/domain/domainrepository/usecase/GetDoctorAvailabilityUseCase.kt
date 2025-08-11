package com.example.eclinic_summer.domain.domainrepository.usecase

import com.example.eclinic_summer.data.model.repository.Availability
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import javax.inject.Inject

class GetDoctorAvailabilityUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(doctorId: String): List<Availability> {
        val doctor = userRepository.getUser(doctorId)
        return doctor?.availability ?: emptyList()
    }
}