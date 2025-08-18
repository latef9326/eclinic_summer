// BookAppointmentUseCase.kt
package com.example.eclinic_summer.domain.domainrepository.usecase

import com.example.eclinic_summer.data.model.Appointment
import kotlinx.coroutines.flow.Flow

interface BookAppointmentUseCase {
    suspend operator fun invoke(appointment: Appointment): Result<Unit>
}