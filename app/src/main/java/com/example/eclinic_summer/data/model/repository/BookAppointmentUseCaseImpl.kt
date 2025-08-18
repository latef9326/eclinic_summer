package com.example.eclinic_summer.data.model.repository

import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import com.example.eclinic_summer.domain.domainrepository.usecase.BookAppointmentUseCase
import javax.inject.Inject

class BookAppointmentUseCaseImpl @Inject constructor(
    private val repository: AppointmentRepository
) : BookAppointmentUseCase {
    override suspend fun invoke(appointment: Appointment): Result<Unit> {
        return repository.bookAppointment(appointment)
    }
}