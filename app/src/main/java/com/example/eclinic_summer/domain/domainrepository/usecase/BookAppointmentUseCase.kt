package com.example.eclinic_summer.domain.domainrepository.usecase

import com.example.eclinic_summer.data.model.Appointment

/**
 * Use case for booking a new appointment.
 */
interface BookAppointmentUseCase {
    /**
     * Books an appointment for a doctor and patient.
     *
     * @param appointment Appointment details.
     * @return [Result] indicating success or failure.
     */
    suspend operator fun invoke(appointment: Appointment): Result<Unit>
}
